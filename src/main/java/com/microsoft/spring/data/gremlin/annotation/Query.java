/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.annotation.QueryAnnotation;

/**
 * Annotation to declare Parameterized queries to be defined as String. 
 * Inspired from Spring Neo4j implementation
 *
 * @author Ganesh Guttikonda
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@QueryAnnotation
@Documented
public @interface Query {

    /**
     * Defines the Gremlin query to be executed when the annotated method is called.
     */
    String value() default "";

}
