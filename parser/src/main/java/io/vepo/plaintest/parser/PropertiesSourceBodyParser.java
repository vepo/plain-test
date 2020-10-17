package io.vepo.plaintest.parser;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import io.vepo.plaintest.PropertiesSource;
import io.vepo.plaintest.PropertiesSource.SourceType;

class PropertiesSourceBodyParser extends BodyParser<PropertiesSource> {

    enum PropertiesSourceAttributes {
        file, headers, separator, type
    }

    private Path file;
    private List<String> headers;
    private String separator;
    private SourceType type;

    PropertiesSourceBodyParser(int index) {
        super(index);
        headers = emptyList();
    }

    @Override
    <J extends BodyParser<?>> J acceptChild(J child) {
        throw new ParserException();
    }

    @Override
    void attribute(String key, Object value) {
        try {
            switch (PropertiesSourceAttributes.valueOf(key)) {
                case type:
                    type = SourceType.valueOf(value.toString());
                    break;
                case file:
                    if (value instanceof String) {
                        file = new File((String) value).toPath();
                    } else {
                        throw new ParserException();
                    }
                    break;
                case separator:
                    if (value instanceof String) {
                        separator = (String) value;
                    } else {
                        throw new ParserException();
                    }
                    break;
                case headers:
                    if (value instanceof List<?>) {
                        headers = ((List<?>) value).stream().map(Object::toString).collect(toList());
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
    PropertiesSource build() {
        return PropertiesSource.builder().index(getIndex()).type(type).file(file).separator(separator).headers(headers)
                .build();
    }

}
