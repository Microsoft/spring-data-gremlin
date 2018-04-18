/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.config;

import com.microsoft.spring.data.gremlin.mapping.GremlinMappingContext;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.MappingContextIsNewStrategyFactory;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.support.CachingIsNewStrategyFactory;
import org.springframework.data.support.IsNewStrategyFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public abstract class GremlinConfigurationSupport {

    protected Collection<String> getMappingBasePackages() {
        final Package basePackage = this.getClass().getPackage();

        return Collections.singleton(basePackage == null ? null : basePackage.getName());
    }

    @SneakyThrows
    protected Set<Class<?>> scanEntities(@NonNull String basePackage) {
        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        final Set<Class<?>> entitySet = new HashSet<>();
        final ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);

        provider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

        for (final BeanDefinition candidate: provider.findCandidateComponents(basePackage)) {
            final String className = candidate.getBeanClassName();
            Assert.notNull(GremlinConfigurationSupport.class.getClassLoader(), "Class loader cannot be null");

            entitySet.add(ClassUtils.forName(className, GremlinConfigurationSupport.class.getClassLoader()));
        }

        return entitySet;
    }

    protected Set<Class<?>> getInitialEntitySet() {
        final Set<Class<?>> entitySet = new HashSet<>();

        this.getMappingBasePackages().forEach(basePackage -> entitySet.addAll(this.scanEntities(basePackage)));

        return entitySet;
    }

    @Bean
    public GremlinMappingContext gremlinMappingContext() {
        final GremlinMappingContext context = new GremlinMappingContext();

        context.setInitialEntitySet(this.getInitialEntitySet());

        return context;
    }

    @Bean
    public IsNewStrategyFactory isNewStrategyFactory() {
        final PersistentEntities entities = new PersistentEntities(Arrays.<MappingContext<?, ?>>asList(
                gremlinMappingContext()));

        return new CachingIsNewStrategyFactory(new MappingContextIsNewStrategyFactory(entities));
    }
}

