package es.upm.etsiinf.tfg.juanmahou.phenofhir;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.org.phenopackets.schema.v2.Phenopacket;
import entities.org.phenopackets.schema.v2.core.Individual;
import entities.org.phenopackets.schema.v2.core.MetaData;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PhenoFhirApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(PhenoFhirApplicationTests.class);
    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RepositoryProvider repositoryProvider;

    @Test
    void contextLoads() {
    }

    @Test
    void insertTransaction() throws Exception {
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

        var request = new ObjectMapper().readTree(json);

        // 6) Every entry.response.status == "201"
        var entries = body.path("entry");
        var reqEntries = request.path("entry");
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            var reqEntry = reqEntries.get(i);
            log.info("REQ {}", reqEntry);
            log.info("RES {}", entry);
            assertThat(entry.path("response").path("status").asText())
                    .isEqualTo("201");
        }
    }

    @Test
    @Transactional
    void readPhenopacket() {
        var indivRepo = repositoryProvider.getCrudRepository(
                ResolvableType.forClass(Individual.class));
        var metaRepo = repositoryProvider.getCrudRepository(
                ResolvableType.forClass(MetaData.class));
        var phenoRepo = repositoryProvider.getCrudRepository(
                ResolvableType.forClass(Phenopacket.class));

        // 1) persist the individual and metadata first
        Individual ind = new Individual().setId(new Individual.Key("phenofhir:ind1"));
        indivRepo.save(ind);

        MetaData md = new MetaData()
                .setCreated_by("Author")
                .setCreated(Instant.now())
                .setPhenopacket_schema_version("2.0");
        metaRepo.save(md);

        // 2) now you can safely save the Phenopacket
        Phenopacket pp = new Phenopacket()
                .setId(new Phenopacket.Key("phenofhir:pp1"))
                .setSubject(ind)
                .setMeta_data(md);
        phenoRepo.save(pp);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        // 3) exercise your read endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.valueOf("application/fhir+json")));
        var resp = restTemplate.exchange(
                URI.create("http://localhost:" + port + "/fhir/Composition/phenofhir:pp1"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        log.info("RES: {}", resp.getBody());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
}
