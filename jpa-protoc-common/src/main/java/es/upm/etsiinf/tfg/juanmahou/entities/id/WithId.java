package es.upm.etsiinf.tfg.juanmahou.entities.id;


public interface WithId<K extends Id> {
    Class<K> getIdClass();

    K getId();
    WithId<K> setId(K id);

//    default String getIdAsString() throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
//                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
//        String json = mapper.writeValueAsString(getId());
//        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
//    }
//
//    default WithIdOld<K> setIdFromString(String key) throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
//                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
//        String json = new String(Base64.getDecoder().decode(key), StandardCharsets.UTF_8);
//        return setId(mapper.readValue(json, getIdClass()));
//    }
}
