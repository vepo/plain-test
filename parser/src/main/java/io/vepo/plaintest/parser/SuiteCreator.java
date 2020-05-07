package io.vepo.plaintest.parser;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Assertion;
import io.vepo.plaintest.Properties;
import io.vepo.plaintest.Properties.PropertiesBuilder;
import io.vepo.plaintest.PropertyReference;
import io.vepo.plaintest.Step;
import io.vepo.plaintest.Step.StepBuilder;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.Suite.SuiteBuilder;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteBaseListener;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.AssertionContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.AttributeContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.ExecDirectoryContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.PropertiesContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.StepContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.SuiteContext;

public class SuiteCreator extends TestSuiteBaseListener {
	private static final Logger logger = LoggerFactory.getLogger(SuiteCreator.class);
	private static final Pattern LINE_START_PATTERN = Pattern.compile("^(\\s+)");
	private SuiteBuilder mainSuite;
	private Deque<SuiteBuilder> suiteQueue;
	private StepBuilder currentStepBuilder;
	private PropertiesBuilder currentPropertiesBuilder;

	public SuiteCreator() {
		mainSuite = null;
		suiteQueue = new LinkedList<>();
	}

	@Override
	public void enterSuite(SuiteContext ctx) {
		if (nonNull(ctx.IDENTIFIER())) {
			logger.trace("Enter Suite: {}", ctx);

			int nextIndex;
			if (suiteQueue.isEmpty()) {
				nextIndex = 0;
			} else {
				nextIndex = suiteQueue.peekLast().nextIndex();
			}
			suiteQueue.addLast(
					Suite.builder().index(nextIndex).name(ctx.IDENTIFIER().getText()).parent(suiteQueue.peekLast()));

			if (isNull(mainSuite)) {
				mainSuite = suiteQueue.peekLast();
			}
		}
	}

	@Override
	public void exitSuite(SuiteContext ctx) {
		if (!ctx.isEmpty()) {
			logger.trace("Exit Suite: {}", ctx);

			Suite builtSuite = suiteQueue.pollLast().build();

			if (!suiteQueue.isEmpty()) {
				suiteQueue.peekLast().child(builtSuite);
			}
		}
	}

	public Suite getTestSuite() {
		return Optional.ofNullable(mainSuite).map(SuiteBuilder::build).orElse(null);
	}

	@Override
	public void enterStep(StepContext ctx) {
		logger.trace("Enter Step: {}", ctx);

		int lastIndex = suiteQueue.peekLast().nextIndex();
		if (ctx.IDENTIFIER().size() == 2) {
			currentStepBuilder = Step.builder().index(lastIndex).plugin(ctx.IDENTIFIER(0).getText())
					.name(ctx.IDENTIFIER(1).getText()).parent(suiteQueue.peekLast());
		}
	}

	@Override
	public void exitStep(StepContext ctx) {
		logger.trace("Exit Step: {}", ctx);
		suiteQueue.peekLast().child(currentStepBuilder.build());
		currentStepBuilder = null;
	}

	private void consumeAttribute(AttributeContext ctx, BiConsumer<String, Object> consumer) {
		if (nonNull(ctx.value())) {
			if (nonNull(ctx.value().STRING())) {
				consumer.accept(ctx.IDENTIFIER().getText(), processString(ctx.value().getText()));
			} else if (nonNull(ctx.value().MULTILINE_STRING())) {
				consumer.accept(ctx.IDENTIFIER().getText(), processMultiLineString(ctx.value().getText()));
			} else if (nonNull(ctx.value().NUMBER())) {
				consumer.accept(ctx.IDENTIFIER().getText(), Long.valueOf((ctx.value().getText())));
			}
		} else if (nonNull(ctx.propertyReference())) {
			consumer.accept(ctx.IDENTIFIER().getText(),
					new PropertyReference(ctx.propertyReference().IDENTIFIER().getText()));
		}
	}

	@Override
	public void exitAttribute(AttributeContext ctx) {
		logger.trace("Exit Attribute: {}", ctx);

		if (ctx.getParent() instanceof StepContext) {
			consumeAttribute(ctx, currentStepBuilder::attribute);
		} else if (ctx.getParent() instanceof PropertiesContext) {
			consumeAttribute(ctx, currentPropertiesBuilder::value);
		}
	}

	private String processString(String text) {
		return unescapeJava(text.substring(1, text.length() - 1));
	}

	private String processMultiLineString(String text) {
		String[] lines = text.substring(3, text.length() - 3).split("\n");
		if (lines.length > 0) {

			if (lines[0].trim().isEmpty()) {
				lines = Arrays.copyOfRange(lines, 1, lines.length);
			}

			if (lines[lines.length - 1].trim().isEmpty()) {
				lines = Arrays.copyOfRange(lines, 0, lines.length - 1);
			}

			removeTabs(lines);
		}
		return Stream.of(lines).collect(Collectors.joining("\n"));
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
			currentStepBuilder.assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(),
					processString(ctx.value().getText())));
		} else if (nonNull(ctx.value().MULTILINE_STRING())) {
			currentStepBuilder.assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(),
					processMultiLineString(ctx.value().getText())));
		} else if (nonNull(ctx.value().NUMBER())) {
			currentStepBuilder.assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(),
					Long.valueOf((ctx.value().getText()))));
		} else if (nonNull(ctx.value().NULL())) {
			currentStepBuilder.assertion(new Assertion<>(ctx.IDENTIFIER().getText(), ctx.VERB().getText(), null));
		}
	}

	@Override
	public void enterExecDirectory(ExecDirectoryContext ctx) {
		logger.trace("Enter ExecDirectory: {}", ctx);
	}

	@Override
	public void exitExecDirectory(ExecDirectoryContext ctx) {
		logger.trace("Exit ExecDirectory: {}", ctx);
		if (nonNull(ctx.FILE_PATH())) {
			suiteQueue.peekLast().attribute(EXECUTION_PATH, ctx.FILE_PATH().toString());
		} else if (nonNull(ctx.IDENTIFIER())) {
			suiteQueue.peekLast().attribute(EXECUTION_PATH, ctx.IDENTIFIER().toString());
		}
	}

	@Override
	public void enterProperties(PropertiesContext ctx) {
		logger.trace("Enter Properties: {}", ctx);
		currentPropertiesBuilder = Properties.builder().parent(suiteQueue.peekLast());
	}

	@Override
	public void exitProperties(PropertiesContext ctx) {
		logger.trace("Exit Properties: {}", ctx);
		suiteQueue.peekLast().child(currentPropertiesBuilder.build());
		currentPropertiesBuilder = null;
	}

}