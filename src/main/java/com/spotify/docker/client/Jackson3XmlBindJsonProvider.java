package com.spotify.docker.client;

import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import tools.jackson.jakarta.rs.cfg.JakartaRSFeature;
import tools.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;

/**
 * {@link JacksonXmlBindJsonProvider} doesn't define any {@link Priority}, so it defaults to {@link Priorities#USER}.
 * <p>
 * Our implementation will always be called first, because it has exactly the same capabilities as {@link JacksonXmlBindJsonProvider}
 * and defines higher priority (lower values have precedence).
 */
@Priority(Priorities.ENTITY_CODER)
@Provider
public class Jackson3XmlBindJsonProvider extends JacksonXmlBindJsonProvider {

    @Inject
    public Jackson3XmlBindJsonProvider() {
        super(ObjectMapperProvider.createObjectMapper(), new JakartaXmlBindAnnotationIntrospector());
        disable(JakartaRSFeature.ALLOW_EMPTY_INPUT);
    }
}
