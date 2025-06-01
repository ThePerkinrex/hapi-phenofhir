package es.upm.etsiinf.tfg.juanmahou.phenofhir.pheno;

import com.google.protobuf.InvalidProtocolBufferException;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import org.phenopackets.schema.v2.Cohort;
import org.phenopackets.schema.v2.Family;
import org.phenopackets.schema.v2.Phenopacket;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/pheno")
public class PhenoController {
    private final CrudRepository<entities.org.phenopackets.schema.v2.Phenopacket,
            entities.org.phenopackets.schema.v2.Phenopacket.Key> phenoRepo;
    private final CrudRepository<entities.org.phenopackets.schema.v2.Family,
            entities.org.phenopackets.schema.v2.Family.Key> familyRepo;
    private final CrudRepository<entities.org.phenopackets.schema.v2.Cohort,
            entities.org.phenopackets.schema.v2.Cohort.Key> cohortRepo;

    public PhenoController(RepositoryProvider repositoryProvider) {
        this.phenoRepo =
                repositoryProvider.getCrudRepository(ResolvableType.forClass(entities.org.phenopackets.schema.v2.Phenopacket.class));
        this.familyRepo =
                repositoryProvider.getCrudRepository(ResolvableType.forClass(entities.org.phenopackets.schema.v2.Family.class));
        this.cohortRepo =
                repositoryProvider.getCrudRepository(ResolvableType.forClass(entities.org.phenopackets.schema.v2.Cohort.class));
    }

    @GetMapping(path = "/Phenopacket/{id}",
            produces = {
                    "application/x-protobuf",      // ProtobufHttpMessageConverter
                    "application/json",            // ProtobufJsonFormatHttpMessageConverter
            })
    public Phenopacket getPhenopacket(@PathVariable("id") String id) throws InvalidProtocolBufferException {
        Optional<entities.org.phenopackets.schema.v2.Phenopacket> pheno = phenoRepo.findById(new entities.org.phenopackets.schema.v2.Phenopacket.Key(id));
        if(pheno.isPresent()) return pheno.get().asPheno();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Phenopacket with id '" + id + "' not found");
    }



    @GetMapping(path = "/Family/{id}",
            produces = {
                    "application/x-protobuf",      // ProtobufHttpMessageConverter
                    "application/json",            // ProtobufJsonFormatHttpMessageConverter
            })
    public Family getFamily(@PathVariable("id") String id) throws InvalidProtocolBufferException {
        Optional<entities.org.phenopackets.schema.v2.Family> pheno = familyRepo.findById(new entities.org.phenopackets.schema.v2.Family.Key(id));
        if(pheno.isPresent()) return pheno.get().asPheno();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Family with id '" + id + "' not found");
    }

    @GetMapping(path = "/Cohort/{id}",
            produces = {
                    "application/x-protobuf",      // ProtobufHttpMessageConverter
                    "application/json",            // ProtobufJsonFormatHttpMessageConverter
            })
    public Cohort getCohort(@PathVariable("id") String id) throws InvalidProtocolBufferException {
        Optional<entities.org.phenopackets.schema.v2.Cohort> pheno = cohortRepo.findById(new entities.org.phenopackets.schema.v2.Cohort.Key(id));
        if(pheno.isPresent()) return pheno.get().asPheno();
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohort with id '" + id + "' not found");
    }
}
