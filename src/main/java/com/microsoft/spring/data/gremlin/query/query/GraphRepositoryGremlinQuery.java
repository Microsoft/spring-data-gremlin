/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.query.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.lang.NonNull;

import com.microsoft.spring.data.gremlin.common.GremlinUtils;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.spring.data.gremlin.query.GremlinOperations;
import com.microsoft.spring.data.gremlin.query.GremlinTemplate;
import com.microsoft.spring.data.gremlin.query.paramerter.GremlinParameterAccessor;
import com.microsoft.spring.data.gremlin.query.paramerter.GremlinParametersParameterAccessor;

public class GraphRepositoryGremlinQuery extends AbstractGremlinQuery {

    private final GremlinQueryMethod method;
    private final GremlinOperations operations;
    private final Client gremlinClient;

    public GraphRepositoryGremlinQuery(@NonNull Client gremlinClient, @NonNull GremlinQueryMethod method,
            @NonNull GremlinOperations operations) {
        super(method, operations);
        this.gremlinClient = gremlinClient;
        this.method = method;
        this.operations = operations;
    }

    @Override
    protected GremlinQuery createQuery(GremlinParameterAccessor accessor) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object execute(@NonNull Object[] parameters) {
        final String query = method.getQuery();
        final Map<String, Object> params = this.resolveParams(this.method.getParameters(), parameters);
        final GremlinParameterAccessor accessor = new GremlinParametersParameterAccessor(this.method, parameters);
        final ResultProcessor processor = method.getResultProcessor().withDynamicProjection(accessor);
        final Class<?> methodReturnType = processor.getReturnedType().getDomainType();
        final ResultSet rs = this.gremlinClient.submit(query, params);
        
        if (ResultSet.class.equals(methodReturnType)) {
            return rs;
        }

        final GremlinSource<?> source = GremlinUtils.toGremlinSource(methodReturnType);
        if (GremlinTemplate.class.equals(this.operations.getClass())) {
            try {
                final List<Result> gremlinResults = rs.all().get();
                final List<?> results = ((GremlinTemplate) this.operations).recoverDomainList(source, gremlinResults);
                if (results == null || results.isEmpty()) {
                    //return null for not found results
                    return null;
                }
                
                if (results.size() == 1) {
                    // return pojo instead of list
                    return results.get(0);
                }
                return results;
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }

        throw new UnsupportedOperationException(methodReturnType + " is not handled by deserializer!");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Map<String, Object> resolveParams(Parameters<?, ?> methodParameters, Object[] parameters) {

        final Map<String, Object> resolvedParameters = new HashMap<>();

        for (final Parameter parameter : methodParameters) {
            final int parameterIndex = parameter.getIndex();
            final Object parameterValue = parameters[parameterIndex];
            // Convenience! Client can simply pass Map<String, Object> params,
            // we automatically resolve them to individual parameters.
            // this is to allow the pass through for GremlinClient
            if (parameterValue instanceof Map) {
                resolvedParameters.putAll((Map) parameterValue);
            }
            parameter.getName().ifPresent(parameterName -> resolvedParameters.put(parameterName, parameterValue));

        }
        return resolvedParameters;
    }

    @Override
    @NonNull
    public GremlinQueryMethod getQueryMethod() {
        return this.method;
    }

}
