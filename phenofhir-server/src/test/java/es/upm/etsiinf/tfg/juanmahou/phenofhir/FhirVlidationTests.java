package es.upm.etsiinf.tfg.juanmahou.phenofhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.org.phenopackets.schema.v2.Phenopacket;
import entities.org.phenopackets.schema.v2.core.Individual;
import entities.org.phenopackets.schema.v2.core.MetaData;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.CurieManager;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import org.hl7.fhir.r4.model.Composition;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FhirVlidationTests {
    private static final Logger log = LoggerFactory.getLogger(FhirVlidationTests.class);
    @LocalServerPort
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RepositoryProvider repositoryProvider;

    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private CurieManager curieManager;

    @Autowired
    private IValidatorModule module;

    @Test
    @Transactional
    void testFhirValidity() throws IOException, URISyntaxException {
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
        Composition comp = fhirContext.newJsonParser().parseResource(Composition.class, resp.getBody());
        FhirValidator validator = fhirContext.newValidator();
        validator.registerValidatorModule(module);
        var result = validator.validateWithResult(comp);
        for(var m : result.getMessages()) {
            if(m.getSeverity() == ResultSeverityEnum.ERROR)
                log.error("VAL: {}", m);
            else
                log.warn("VAL: {}", m);
        }
        assertThat(result.isSuccessful()).isTrue();
    }

    @Test
    @Transactional
    void testFhirInfo() throws IOException, URISyntaxException {
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
        Composition comp = fhirContext.newJsonParser().parseResource(Composition.class, resp.getBody());

        assertThat(comp.getSubject().getReference()).isEqualTo(ind.getId().getId());
        assertThat(comp.getIdentifier().getSystem()).isEqualTo(curieManager.getOwnSystem().system());
        assertThat(comp.getIdentifier().getValue()).isEqualTo("pp1");

        // FIXME
        // Everything else is missing
    }
}
