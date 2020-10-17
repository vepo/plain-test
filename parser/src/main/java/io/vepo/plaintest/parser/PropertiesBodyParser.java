package io.vepo.plaintest.parser;

import java.util.HashMap;
import java.util.Map;

import io.vepo.plaintest.Properties;

class PropertiesBodyParser extends BodyParser<Properties> {

    private Map<String, Object> values;

    PropertiesBodyParser(int index) {
        super(index);
        values = new HashMap<>();
    }

    @Override
    <J extends BodyParser<?>> J acceptChild(J child) {
        throw new ParserException();
    }

    @Override
    void attribute(String key, Object value) {
        values.put(key, value);
    }

    @Override
    Properties build() {
        return Properties.builder().index(getIndex()).values(values).build();
    }

}
