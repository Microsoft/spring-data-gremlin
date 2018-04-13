/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.query;

/**
 * Provider interface for basic Operations with Gremlin
 */
public interface GremlinOperations {

    void deleteAll();

    <T> T insert(T object);

    <T> T findVertexById(Object id, Class<T> domainClass);

    <T>T findEdgeById(Object id, Class<T> domainClass);
}
