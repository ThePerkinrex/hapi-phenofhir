# PhenoFHIR

This is a HAPI FHIR server implementation backed by a relational database built from the Phenopacket model.

The code is divided in 4 modules:
* jpa-protoc-common:

  This contains all the common interfaces between the JPA `protoc` plugin that builds the ORM and the actual server
* jpa-protoc-plugin:

  This contains the actual `protoc` plugin that builds the ORM from the protobuf definitions.
* mapper:

  This contains the base translation infrastructure.
* phenofhir-server:

  This contains the actual server implementation, with a Java Beans validated config, validation of FHIR resources, and the mappers required for the minimal configuration, which is not complete, to work.
