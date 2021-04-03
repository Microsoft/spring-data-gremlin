/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion.result;

import com.microsoft.spring.data.gremlin.common.Constants;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
// TODO: seems only for Vertex.
public abstract class AbstractGremlinResultReader {

    /**
     * properties's organization is a little complicated.
     * <p>
     * properties is Map<K, V>
     * K is String
     * V is ArrayList<T>
     * T is Map<String, String>
     */
    private Object readProperty(@NonNull Object value) {
        Assert.isInstanceOf(ArrayList.class, value, "should be instance of ArrayList");

        final List listValue = (List) value;
        Assert.isTrue(listValue.size() == 1, "should be only 1 element in ArrayList");
        final Map map = (Map) listValue.get(0);

        return map.get(Constants.PROPERTY_VALUE);
    }

    protected void readResultProperties(@NonNull Map<String, Object> properties, @NonNull GremlinSource source) {
        source.getProperties().clear();
        properties.forEach((key, value) -> source.setProperty(key, this.readProperty(value)));
    }
}
