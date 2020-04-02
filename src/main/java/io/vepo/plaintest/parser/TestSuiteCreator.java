package io.vepo.plaintest.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.vepo.plaintest.TestSuite;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteListener;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser.TestSuiteContext;

public class TestSuiteCreator implements TestSuiteListener {
	private TestSuite testSuite;
	private TestSuite currentTestSuite;

	public TestSuiteCreator() {
		this.currentTestSuite = this.testSuite = new TestSuite();
	}

	@Override
	public void visitTerminal(TerminalNode node) {

	}

	@Override
	public void visitErrorNode(ErrorNode node) {

	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {

	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {

	}

	@Override
	public void enterTestSuite(TestSuiteContext ctx) {
		this.currentTestSuite.setName(ctx.IDENTIFIER().getText());

	}

	@Override
	public void exitTestSuite(TestSuiteContext ctx) {

	}

	public TestSuite getTestSuite() {
		return testSuite;
	}

}
