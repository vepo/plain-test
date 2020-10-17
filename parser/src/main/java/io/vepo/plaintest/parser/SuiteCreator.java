package io.vepo.plaintest.parser;

import static java.util.Arrays.copyOfRange;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Assertion;
import io.vepo.plaintest.PropertyReference;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteBaseListener;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.AssertionContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.AttributeContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.StepBodyContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.SuiteBodyContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.TypedBodyContext;

public class SuiteCreator extends TestSuiteBaseListener {
    private static final Logger logger = LoggerFactory.getLogger(SuiteCreator.class);
    private static final Pattern LINE_START_PATTERN = Pattern.compile("^(\\s+)");
    private SuiteBodyParser mainSuite;
    private Deque<BodyParser<?>> bodyQueue;

    public SuiteCreator() {
        mainSuite = null;
        bodyQueue = new LinkedList<>();
    }

    @Override
    public void enterSuiteBody(SuiteBodyContext ctx) {
        if (nonNull(ctx.IDENTIFIER())) {
            logger.trace("Enter Suite: {}", ctx);

            BodyParser<?> parent = bodyQueue.peekLast();
            if (nonNull(parent) && !(parent instanceof SuiteBodyParser)) {
                throw new ParserException();
            }

            int nextIndex;
            if (bodyQueue.isEmpty()) {
                nextIndex = 0;
            } else {
                nextIndex = parent.nextIndex();
            }
            SuiteBodyParser suite = BodyParser.suite(nextIndex, ctx.IDENTIFIER().getText(), parent);
            bodyQueue.addLast(suite);

            if (isNull(mainSuite)) {
                mainSuite = suite;
            }
        }
    }

    @Override
    public void exitSuiteBody(SuiteBodyContext ctx) {
        if (!ctx.isEmpty()) {
            logger.trace("Exit Suite: {}", ctx);
            bodyQueue.removeLast();
        }
    }

    public Suite getTestSuite() {
        return Optional.ofNullable(mainSuite).map(SuiteBodyParser::build).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void enterStepBody(StepBodyContext ctx) {
        logger.trace("Enter Step: {}", ctx);
        bodyQueue.addLast(BodyParser.step(bodyQueue.peekLast().nextIndex(), ctx.IDENTIFIER(0).getText(),
                ctx.IDENTIFIER(1).getText(), (BodyParser<Suite>) bodyQueue.peekLast()));
    }

    @Override
    public void exitStepBody(StepBodyContext ctx) {
        logger.trace("Exit Step: {}", ctx);
        bodyQueue.removeLast();
    }

    @Override
    public void enterTypedBody(TypedBodyContext ctx) {
        bodyQueue.addLast(
                BodyParser.typed(bodyQueue.peekLast().nextIndex(), ctx.TYPE().getText(), bodyQueue.peekLast()));
    }

    @Override
    public void exitTypedBody(TypedBodyContext ctx) {
        logger.trace("Exit Typed: {}", ctx);
        bodyQueue.removeLast();
    }

    @Override
    public void exitAttribute(AttributeContext ctx) {
        logger.trace("Exit Attribute: {}", ctx);
        if (nonNull(ctx.value())) {
            if (nonNull(ctx.value().STRING())) {
                bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(), processString(ctx.value().getText()));
            } else if (nonNull(ctx.value().MULTILINE_STRING())) {
                bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(),
                        processMultiLineString(ctx.value().getText()));
            } else if (nonNull(ctx.value().NUMBER())) {
                bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(), Long.valueOf((ctx.value().getText())));
            } else if (nonNull(ctx.value().BOOLEAN())) {
                bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(), Boolean.valueOf((ctx.value().getText())));
            } else if (nonNull(ctx.value().FILE_PATH())) {
                bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(), ctx.value().FILE_PATH().getText());
            } else if (nonNull(ctx.value().IDENTIFIER())) {
                bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(), ctx.value().IDENTIFIER().getText());
            }
        } else if (nonNull(ctx.propertyReference())) {
            bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(),
                    new PropertyReference(ctx.propertyReference().IDENTIFIER().getText()));
        } else if (nonNull(ctx.identifierList())) {
            bodyQueue.peekLast().attribute(ctx.IDENTIFIER().getText(),
                    ctx.identifierList().IDENTIFIER().stream().map(TerminalNode::getText).collect(toList()));
        }
    }

    private String processString(String text) {
        return unescapeJava(text.substring(1, text.length() - 1));
    }

    private String processMultiLineString(String text) {
        String[] lines = text.substring(3, text.length() - 3).split("\n");
        if (lines.length > 0) {

            if (lines[0].trim().isEmpty()) {
                lines = copyOfRange(lines, 1, lines.length);
            }

            if (lines[lines.length - 1].trim().isEmpty()) {
                lines = copyOfRange(lines, 0, lines.length - 1);
            }

            removeTabs(lines);
            removeCarriageReturn(lines);
        }
        return Stream.of(lines).collect(Collectors.joining("\n"));
    }

    private void removeCarriageReturn(String[] lines) {
        IntStream.range(0, lines.length).forEach(index -> lines[index] = lines[index].replaceAll("\\s+$", ""));
    }

    private void removeTabs(String[] lines) {
        Matcher tabMatcher = LINE_START_PATTERN.matcher(lines[0]);
        if (tabMatcher.find()) {
            String tabPattern = tabMatcher.group(1);
            range(0, lines.length).filter(index -> lines[index].startsWith(tabPattern))
                    .forEach(index -> lines[index] = lines[index].substring(tabPattern.length()));
        }
    }

    @Override
    public void exitAssertion(AssertionContext ctx) {
        logger.trace("Enter Assertion: {}", ctx);
        if (nonNull(ctx.value().STRING())) {
            bodyQueue.peekLast().assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(),
                    processString(ctx.value().getText())));
        } else if (nonNull(ctx.value().MULTILINE_STRING())) {
            bodyQueue.peekLast().assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(),
                    processMultiLineString(ctx.value().getText())));
        } else if (nonNull(ctx.value().NUMBER())) {
            bodyQueue.peekLast().assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(),
                    Long.valueOf((ctx.value().getText()))));
        } else if (nonNull(ctx.value().NULL())) {
            bodyQueue.peekLast().assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(), null));
        }
    }

}