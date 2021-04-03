/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.repository;

import com.microsoft.spring.data.gremlin.common.GremlinEntityType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface GremlinRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

    Iterable<T> findAll(Class<T> domainClass);

    Iterable<T> findAllByQuery(List<String> queryList);

    void deleteAll(GremlinEntityType type);

    void deleteAll(Class<T> domainClass);

    long vertexCount();

    long edgeCount();
}
