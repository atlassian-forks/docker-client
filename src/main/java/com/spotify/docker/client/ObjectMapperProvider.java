package com.spotify.docker.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.datatype.guava.GuavaModule;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

import jakarta.ws.rs.ext.ContextResolver;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private static final SimpleModule MODULE = new SimpleModule();

    static {
        MODULE.addSerializer(Set.class, new SetSerializer());
        MODULE.addDeserializer(Set.class, new SetDeserializer());
        MODULE.addSerializer(ImmutableSet.class, new ImmutableSetSerializer());
        MODULE.addDeserializer(ImmutableSet.class, new ImmutableSetDeserializer());
    }

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    public ObjectMapperProvider() {
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return OBJECT_MAPPER;
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    private static AnnotationIntrospector createJacksonJaxbAnnotationIntrospector() {
        final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        final AnnotationIntrospector jaxbIntrospector = new JakartaXmlBindAnnotationIntrospector();
        return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
    }

    public static JsonMapper createObjectMapper() {
        JsonFactory jsonFactory = JsonFactory.builderWithJackson2Defaults()
                .build();

        return JsonMapper.builder(jsonFactory)
                .configureForJackson2()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL).withValueInclusion(JsonInclude.Include.NON_NULL))
                .findAndAddModules()
                .addModule(new GuavaModule())
                .addModule(MODULE)
                .annotationIntrospector(createJacksonJaxbAnnotationIntrospector())
                .build();
    }



    private static final Function<? super Object, ?> EMPTY_MAP = (Function<Object, Object>) input -> Collections.emptyMap();

    private static class SetSerializer extends ValueSerializer<Set> {
        @Override
        public void serialize(Set value, tools.jackson.core.JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            final Map map = (value == null) ? null : Maps.asMap(value, EMPTY_MAP);
            ctxt.writeValue(gen, map);
        }

        @Override
        public Class<?> handledType() {
            return Set.class;
        }
    }

    private static class SetDeserializer extends ValueDeserializer<Set> {
        @Override
        public Set deserialize(tools.jackson.core.JsonParser jp, tools.jackson.databind.DeserializationContext ctxt) throws JacksonException {
            final Map map = ctxt.readValue(jp, Map.class);
            return (map == null) ? null : map.keySet();
        }

        @Override
        public Class<?> handledType() {
            return Set.class;
        }
    }

    private static class ImmutableSetSerializer extends ValueSerializer<ImmutableSet> {

        @Override
        public void serialize(ImmutableSet value, tools.jackson.core.JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            final Map map = (value == null) ? null : Maps.asMap(value, EMPTY_MAP);
            ctxt.writeValue(gen, map);
        }

        @Override
        public Class<?> handledType() {
            return ImmutableSet.class;
        }
    }

    private static class ImmutableSetDeserializer extends ValueDeserializer<ImmutableSet> {
        @Override
        public ImmutableSet deserialize(tools.jackson.core.JsonParser jp, tools.jackson.databind.DeserializationContext ctxt) throws JacksonException {
            final Map map = ctxt.readValue(jp, Map.class);
            return (map == null) ? null : ImmutableSet.copyOf(map.keySet());
        }

        @Override
        public Class<?> handledType() {
            return ImmutableSet.class;
        }
    }
}