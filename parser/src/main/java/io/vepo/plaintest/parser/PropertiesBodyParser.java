package io.vepo.plaintest.parser;

import java.util.HashMap;
import java.util.Map;

import io.vepo.plaintest.Properties;

class PropertiesBodyParser extends BodyParser<Properties> {

    private Map<String, Object> values;

    public PropertiesBodyParser(int index) {
        super(index);
        values = new HashMap<>();
    }

    @Override
    void attribute(String key, Object value) {
        values.put(key, value);
    }

    @Override
    Properties construct() {
        return Properties.builder().index(getIndex()).values(values).build();
    }

    @Override
    <J extends BodyParser<?>> J acceptChild(J child) {
        throw new ParserException();
    }

}
