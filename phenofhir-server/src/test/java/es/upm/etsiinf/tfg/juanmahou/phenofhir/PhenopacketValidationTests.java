package es.upm.etsiinf.tfg.juanmahou.phenofhir;


import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.org.phenopackets.schema.v2.Phenopacket;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import org.junit.jupiter.api.Test;
import org.phenopackets.phenopackettools.validator.core.ValidationWorkflowRunner;
import org.phenopackets.phenopackettools.validator.jsonschema.JsonSchemaValidationWorkflowRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PhenopacketValidationTests {
    private static final Logger log = LoggerFactory.getLogger(PhenoFhirApplicationTests.class);
    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RepositoryProvider repositoryProvider;

    @Autowired
    private FhirContext fhirContext;

    @Test
    @Transactional
    void testPhenopacket() throws IOException, URISyntaxException {
        // 1) Load your transaction JSON
        String json = Files.readString(
                Paths.get(Objects.requireNonNull(
                                getClass().getClassLoader()
                                        .getResource("ig-examples/transaction.json"))
                        .toURI()),
                StandardCharsets.UTF_8
        );

        // 2) Build headers for FHIR+JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/fhir+json"));
        headers.setAccept(List.of(MediaType.valueOf("application/fhir+json")));

        // 3) POST to the live server
        var resp = restTemplate.exchange(
                URI.create("http://localhost:" + port + "/fhir/"),
                HttpMethod.POST,
                new HttpEntity<>(json, headers),
                String.class
        );

        String bodyStr = resp.getBody();
        log.info("BODY: {}", bodyStr);

        // 4) Basic HTTP + Content-Type checks
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(resp.getHeaders().getContentType()).toString()).startsWith("application" +
                "/fhir+json");

        // 5) Parse the JSON body and assert FHIR transaction-response
        var body = new ObjectMapper().readTree(bodyStr);
        assertThat(body.path("resourceType").asText())
                .isEqualTo("Bundle");
        assertThat(body.path("type").asText())
                .isEqualTo("transaction-response");

        CrudRepository<Phenopacket, Phenopacket.Key> repo = repositoryProvider.getCrudRepository(ResolvableType.forClass(Phenopacket.class));
        Optional<Phenopacket> pheno = repo.findById(new Phenopacket.Key("phenofhir:composition"));
        assertThat(pheno).isPresent();
        org.phenopackets.schema.v2.Phenopacket p = pheno.get().asPheno();
        log.info("Loaded: {}", p);

        ValidationWorkflowRunner<org.phenopackets.schema.v2.PhenopacketOrBuilder> runner = JsonSchemaValidationWorkflowRunner.phenopacketBuilder().build();
        var results = runner.validate(p);
        for(var r : results.validationResults()) {
            log.info("Validator   {}", r.validatorInfo().validatorName());
            log.info("Description {}", r.validatorInfo().description());
            log.info("{}", r.message());
        }
        assertThat(results.isValid()).isTrue();


    }
}
