/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion.source;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

public class GremlinSourceEdge extends BasicGremlinSource {

    @Getter
    @Setter
    private String vertexIdFrom;

    @Getter
    @Setter
    private String vertexIdTo;

    public GremlinSourceEdge() {
        super();
    }

    public GremlinSourceEdge(@NonNull String id) {
        super();
        super.setId(id);
    }
}
