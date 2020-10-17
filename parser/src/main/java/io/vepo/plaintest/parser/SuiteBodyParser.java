package io.vepo.plaintest.parser;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Path;

import io.vepo.plaintest.Suite;

class SuiteBodyParser extends BodyParser<Suite> {

    private String name;
    private Path path;

    SuiteBodyParser(int index, String name) {
        super(index);
        this.name = name;
    }

    @Override
    <J extends BodyParser<?>> J acceptChild(J child) {
        addChild(child);
        return child;
    }

    @Override
    void attribute(String key, Object value) {
        try {
            switch (key) {
                case "path":
                    if (value instanceof String) {
                        path = new File((String) value).toPath();
                    } else {
                        throw new ParserException();
                    }
                    break;
                default:
                    throw new ParserException();
            }
        } catch (IllegalArgumentException iae) {
            throw new ParserException();
        }
    }

    @Override
    Suite build() {
        return Suite.builder().index(getIndex()).name(name).executionPath(path)
                .children(getChildren().stream().map(BodyParser::build).collect(toList())).build();
    }
}
