/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion.source;

import com.microsoft.spring.data.gremlin.annotation.EdgeSet;
import com.microsoft.spring.data.gremlin.annotation.VertexSet;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.microsoft.spring.data.gremlin.mapping.GremlinPersistentEntity;
import com.microsoft.spring.data.gremlin.repository.support.GremlinEntityInformation;
import lombok.NoArgsConstructor;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.List;

@NoArgsConstructor
public class GremlinSourceGraphWriter implements GremlinSourceWriter {

    @SuppressWarnings("unchecked")
    private void writeGraphSet(@NonNull List<Object> objectList, @NonNull MappingGremlinConverter mappingConverter,
                               @NonNull GremlinSourceGraph sourceGraph) {
        Assert.isInstanceOf(GremlinSourceGraph.class, sourceGraph, "should be instance of GremlinSourceGraph ");

        for (final Object object : objectList) {
            final GremlinEntityInformation information = new GremlinEntityInformation(object.getClass());
            final GremlinSource source = information.getGremlinSource();

            source.doGremlinSourceWrite(object, mappingConverter);
            sourceGraph.addGremlinSource(source);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(@NonNull Object domain, @NonNull MappingGremlinConverter converter,
                      @NonNull GremlinSource source) {
        if (!(source instanceof GremlinSourceGraph)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final GremlinSourceGraph sourceGraph = (GremlinSourceGraph) source;
        final GremlinPersistentEntity<?> persistentEntity = converter.getPersistentEntity(domain.getClass());
        final ConvertingPropertyAccessor accessor = converter.getPropertyAccessor(domain);

        for (final Field field : domain.getClass().getDeclaredFields()) {
            final PersistentProperty property = persistentEntity.getPersistentProperty(field.getName());
            Assert.notNull(property, "persistence property should not be null");
            final List<Object> objectList = (List<Object>) accessor.getProperty(property);

            if (field.getAnnotation(VertexSet.class) != null || field.getAnnotation(EdgeSet.class) != null) {
                this.writeGraphSet(objectList, converter, sourceGraph);
            }
        }
    }
}

