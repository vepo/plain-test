package io.vepo.plaintest.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.vepo.plaintest.Suite;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteLexer;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser;

public class TestSuiteFactory {
	private TestSuiteFactory() {
	}

	public static Suite parseSuite(String contents) {
		var parser = new TestSuiteParser(new CommonTokenStream(new TestSuiteLexer(CharStreams.fromString(contents))));
		ParseTreeWalker walker = new ParseTreeWalker();
		TestSuiteCreator creator = new TestSuiteCreator();
		walker.walk(creator, parser.suite());
		return creator.getTestSuite();
	}
}
