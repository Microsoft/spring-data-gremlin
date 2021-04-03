/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.query.query;

import com.microsoft.spring.data.gremlin.annotation.Query;
import com.microsoft.spring.data.gremlin.query.GremlinEntityMetadata;
import com.microsoft.spring.data.gremlin.query.SimpleGremlinEntityMetadata;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

public class GremlinQueryMethod extends QueryMethod {

    private GremlinEntityMetadata<?> metadata;
    private final Query queryAnnotation;
    private final Method method;

    public GremlinQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
        this.queryAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, Query.class);
        this.method = method;
    }

    @Override
    public EntityMetadata<?> getEntityInformation() {
        @SuppressWarnings("unchecked") final Class<Object> domainClass = (Class<Object>) super.getDomainClass();

        this.metadata = new SimpleGremlinEntityMetadata<>(domainClass);

        return this.metadata;
    }
    
    public String getQuery() {
        return queryAnnotation.value();
    }

    public boolean hasAnnotatedQuery() {
        return getAnnotatedQuery() != null;
    }

    private String getAnnotatedQuery() {

        final String query = (String) AnnotationUtils.getValue(getQueryAnnotation());
        return StringUtils.hasText(query) ? query : null;
    }

    private Query getQueryAnnotation() {
        return AnnotatedElementUtils.findMergedAnnotation(method, Query.class);
    }
}
