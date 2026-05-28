package com.algaworks.algashop.billingscheduler.utility.databuilder;


import tools.jackson.databind.node.ObjectNode;

import static java.util.Objects.isNull;

public interface JsonDataBuilder {

    default void putNullable(final ObjectNode json, final String field, final Object value) {

        if (isNull(value)) {
            json.putNull(field);
            return;
        }

        switch (value) {
            case String v -> json.put(field, v);
            case Integer v -> json.put(field, v);
            case Long v -> json.put(field, v);
            case Double v -> json.put(field, v);
            case Float v -> json.put(field, v);
            case Boolean v -> json.put(field, v);
            default -> json.putPOJO(field, value);
        }
    }

}
