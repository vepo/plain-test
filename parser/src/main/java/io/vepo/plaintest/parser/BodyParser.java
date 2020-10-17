package io.vepo.plaintest.parser;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import io.vepo.plaintest.Assertion;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.TestItem;

abstract class BodyParser<T extends TestItem> {

    static StepBodyParser step(int index, String plugin, String name, BodyParser<Suite> parent) {
        return parent.acceptChild(new StepBodyParser(index, plugin, name));
    }

    static SuiteBodyParser suite(int index, String name, BodyParser<?> parent) {
        if (nonNull(parent)) {
            return parent.acceptChild(new SuiteBodyParser(index, name));
        } else {
            return new SuiteBodyParser(index, name);
        }
    }

    static BodyParser<?> typed(int index, String type, BodyParser<?> parent) {
        try {
            switch (type) {
                case "Parallel":
                    return parent.acceptChild(new ParallelBodyParser(index));
                case "Properties":
                    return parent.acceptChild(new PropertiesBodyParser(index));
                case "PropertiesSource":
                    return parent.acceptChild(new PropertiesSourceBodyParser(index));
            }
        } catch (IllegalArgumentException iae) {
            throw new ParserException();
        }
        throw new ParserException();
    }

    private List<BodyParser<?>> children;
    private int index;

    BodyParser(int index) {
        this.index = index;
        this.children = new ArrayList<>();
    }

    abstract <J extends BodyParser<?>> J acceptChild(J child);

    protected <J extends BodyParser<?>> void addChild(J child) {
        this.children.add(child);
    }

    void assertion(Assertion<?> assertion) {
        throw new ParserException();
    }

    abstract void attribute(String key, Object value);

    abstract T build();

    List<BodyParser<? extends TestItem>> getChildren() {
        return children;
    }

    int getIndex() {
        return index;
    }

    int nextIndex() {
        return children.stream().mapToInt(BodyParser::getIndex).max().orElse(-1) + 1;
    }
}
