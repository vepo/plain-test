package io.vepo.plaintest.parser;

import static java.util.stream.Collectors.toList;

import io.vepo.plaintest.Parallel;

class ParallelBodyParser extends BodyParser<Parallel> {
    enum ParallelAttributes {
        times, rampUp, maxThreads
    }

    private int times;
    private int maxThreads;
    private int rampUp;

    public ParallelBodyParser(int index) {
        super(index);
    }

    @Override
    void attribute(String key, Object value) {
        switch (ParallelAttributes.valueOf(key)) {
            case times:
                if (value instanceof Number) {
                    times = ((Number) value).intValue();
                } else {
                    throw new ParserException();
                }
                break;
            case rampUp:
                if (value instanceof Number) {
                    rampUp = ((Number) value).intValue();
                } else {
                    throw new ParserException();
                }
                break;
            case maxThreads:
                if (value instanceof Number) {
                    maxThreads = ((Number) value).intValue();
                } else {
                    throw new ParserException();
                }
                break;
        }
    }

    @Override
    Parallel construct() {
        return Parallel.builder().index(getIndex()).times(times).rampUp(rampUp).maxThreads(maxThreads)
                .children(getChildren().stream().map(BodyParser::build).collect(toList())).build();
    }

    @Override
    <J extends BodyParser<?>> J acceptChild(J child) {
        addChild(child);
        return child;
    }

}
