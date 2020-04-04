package io.vepo.plaintest.parser;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.TestStep;
import io.vepo.plaintest.TestSuite;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteListener;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.PropertyDefinitionContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.PropertyValueContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.TestStepContext;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.TestSuiteContext;

public class TestSuiteCreator implements TestSuiteListener {
	private static final Logger logger = LoggerFactory.getLogger(TestSuiteCreator.class);

	private TestSuite testSuite;
	private TestSuite currentTestSuite;

	private TestStep currentStep;

	public TestSuiteCreator() {
		this.currentTestSuite = this.testSuite = null;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		logger.debug("Visit Terminal: {}", node);
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		logger.debug("Visit Error Node: {}", node);
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		logger.debug("Enter Every Rule: {}", ctx);
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		logger.debug("Exit Every Rule: {}", ctx);
	}

	@Override
	public void enterTestSuite(TestSuiteContext ctx) {
		logger.debug("Enter Test Suite: {}", ctx);
		this.currentTestSuite = new TestSuite(ctx.IDENTIFIER().getText(), new ArrayList<>(), new ArrayList<>());
		if (isNull(this.testSuite)) {
			this.testSuite = this.currentTestSuite;
		}
	}

	@Override
	public void exitTestSuite(TestSuiteContext ctx) {
		logger.debug("Exit Test Suite: {}", ctx);
	}

	public TestSuite getTestSuite() {
		return testSuite;
	}

	@Override
	public void enterTestStep(TestStepContext ctx) {
		logger.debug("Enter Test Step: {}", ctx);
		this.currentStep = new TestStep(ctx.IDENTIFIER(0).getText(), ctx.IDENTIFIER(1).getText(), new HashMap<>());
		this.currentTestSuite.addStep(currentStep);
	}

	@Override
	public void exitTestStep(TestStepContext ctx) {
		logger.debug("Exit Test Step: {}", ctx);
	}

	@Override
	public void enterPropertyDefinition(PropertyDefinitionContext ctx) {
		logger.debug("Enter Property Definition: {}", ctx);
	}

	@Override
	public void exitPropertyDefinition(PropertyDefinitionContext ctx) {
		logger.debug("Exit Property Definition: {}", ctx);

		if (nonNull(ctx.propertyValue().STRING())) {
			this.currentStep.addStringAttribute(ctx.IDENTIFIER().getText(),
					unescapeJava(removeQuotes(ctx.propertyValue().getText())));
		} else if (nonNull(ctx.propertyValue().NUMBER())) {
			this.currentStep.addNumberAttribute(ctx.IDENTIFIER().getText(),
					Long.valueOf((ctx.propertyValue().getText())));
		} else {
			logger.warn("Invalid value! ctx={}", ctx);
		}
	}

	private String removeQuotes(String text) {
		return text.substring(1, text.length() - 1);
	}

	@Override
	public void enterPropertyValue(PropertyValueContext ctx) {
		logger.debug("Enter Property Value: {}", ctx);
	}

	@Override
	public void exitPropertyValue(PropertyValueContext ctx) {
		logger.debug("Exit Property Value: {}", ctx);
	}

}
