package io.vepo.plaintest;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.vepo.plaintest.parser.SuiteCreator;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteLexer;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser;

public class SuiteFactory {

    private SuiteFactory() {
    }

    public static Suite parseSuite(String contents) {
        TestSuiteParser parser = new TestSuiteParser(
                new CommonTokenStream(new TestSuiteLexer(CharStreams.fromString(contents))));
        ParseTreeWalker walker = new ParseTreeWalker();
        SuiteCreator creator = new SuiteCreator();
        walker.walk(creator, parser.suiteBody());
        return creator.getTestSuite();
    }
}
