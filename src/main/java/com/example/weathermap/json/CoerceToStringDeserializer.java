package com.example.weathermap.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 * Accepts JSON strings or numbers (e.g. WIND_SPEED as 3.7) and maps to {@link String}.
 */
public class CoerceToStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return switch (p.currentToken()) {
            case VALUE_NUMBER_FLOAT -> p.getDecimalValue().toPlainString();
            case VALUE_NUMBER_INT -> String.valueOf(p.getLongValue());
            case VALUE_STRING -> p.getText();
            case VALUE_NULL -> null;
            default -> p.getValueAsString();
        };
    }
}
