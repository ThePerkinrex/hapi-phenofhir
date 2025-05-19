# `ExternalReference`
the ID on this message is optional in the schema, in the database it will be required, as it is application specific, if the ID is missing we will generate one

# `Pedigree.Person`
The ID could be the individual ID, but as this data is taken from PED files, IDs could be the same in different files, so a separate ID is generated. This may be wrong. Also, no foreign keys are inserted. Probably they should be present.