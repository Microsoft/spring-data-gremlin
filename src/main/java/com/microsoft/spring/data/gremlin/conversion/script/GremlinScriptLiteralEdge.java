/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion.script;

import com.microsoft.spring.data.gremlin.common.Constants;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class GremlinScriptLiteralEdge extends BasicGremlinScriptLiteral implements GremlinScriptLiteral {

    @Override
    public String generateInsertScript(@NonNull GremlinSource source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final List<String> scriptList = new ArrayList<>();
        final String label = source.getLabel();
        final String id = source.getId();
        final Map<String, Object> properties = source.getProperties();

        Assert.notNull(label, "label should not be null");
        Assert.notNull(id, "id should not be null");
        Assert.notNull(properties, "properties should not be null");

        final GremlinSourceEdge sourceEdge = (GremlinSourceEdge) source;
        final String vertexIdFrom = sourceEdge.getVertexIdFrom();
        final String vertexIdTo = sourceEdge.getVertexIdTo();

        Assert.notNull(vertexIdFrom, "vertexIdFrom should not be null");
        Assert.notNull(vertexIdTo, "vertexIdTo should not be null");

        scriptList.add(Constants.GREMLIN_PRIMITIVE_GRAPH);

        scriptList.add(String.format(Constants.GREMLIN_PRIMITIVE_VERTEX, vertexIdFrom));
        scriptList.add(String.format(Constants.GREMLIN_PRIMITIVE_ADD_EDGE, label));
        scriptList.add(String.format(Constants.GREMLIN_PRIMITIVE_TO_VERTEX, vertexIdTo));
        scriptList.add(String.format(Constants.GREMLIN_PRIMITIVE_PROPERTY_STRING, Constants.PROPERTY_ID, id));

        super.generateProperties(scriptList, properties);

        return String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, scriptList);
    }

    @Override
    public String generateDeleteAllScript(@Nullable GremlinSource source) {
        return Constants.GREMLIN_SCRIPT_EDGE_DROP_ALL;
    }

    @Override
    public String generateFindByIdScript(@NonNull GremlinSource source) {
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final List<String> scriptList = new ArrayList<>();
        final String id = source.getId();

        Assert.notNull(id, "id should not be null");

        scriptList.add(Constants.GREMLIN_PRIMITIVE_GRAPH);
        scriptList.add(String.format(Constants.GREMLIN_PRIMITIVE_EDGE, id));

        return String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, scriptList);
    }
}
