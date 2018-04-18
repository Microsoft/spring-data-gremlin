/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.config;

import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.query.GremlinTemplate;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.springframework.context.annotation.Bean;

public abstract class AbstractGremlinConfiguration extends GremlinConfigurationSupport {

    public abstract GremlinFactory gremlinFactory();

    @Bean
    public Client gremlinClient() {
        return gremlinFactory().getGremlinClient();
    }

    @Bean
    public MappingGremlinConverter mappingGremlinConverter() throws ClassNotFoundException {
        return new MappingGremlinConverter(gremlinMappingContext());
    }

    @Bean
    public GremlinTemplate gremlinTemplate() throws ClassNotFoundException {
        return new GremlinTemplate(gremlinFactory(), mappingGremlinConverter());
    }
}
