package io.vepo.plaintest.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vepo.plaintest.Assertion;
import io.vepo.plaintest.Step;

public class StepBodyParser extends BodyParser<Step> {

    private String plugin;
    private String name;
    private Map<String, Object> attributes;
    private List<Assertion<?>> assertions;

    StepBodyParser(int index, String plugin, String name) {
        super(index);
        this.plugin = plugin;
        this.name = name;
        attributes = new HashMap<>();
        assertions = new ArrayList<>();
    }

    @Override
    Step construct() {
        return Step.builder().index(getIndex()).plugin(plugin).name(name).attributes(attributes).assertions(assertions)
                .build();
    }

    @Override
    void attribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    <J extends BodyParser<?>> J acceptChild(J child) {
        addChild(child);
        return child;
    }

    @Override
    void assertion(Assertion<?> assertion) {
        assertions.add(assertion);
    }

}
