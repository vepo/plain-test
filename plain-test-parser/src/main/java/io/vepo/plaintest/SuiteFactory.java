package io.vepo.plaintest;

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.parser.SuiteCreator;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteLexer;
import io.vepo.plaintest.parser.antlr4.generated.TestSuiteParser;

public class SuiteFactory {
	private static final Logger logger = LoggerFactory.getLogger(SuiteFactory.class);

	private SuiteFactory() {
	}

	public static Suite parseSuite(String contents) {
		TestSuiteParser parser = new TestSuiteParser(
				new CommonTokenStream(new TestSuiteLexer(CharStreams.fromString(contents))));
		parser.addErrorListener(new ANTLRErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				logger.error("Parser Error: recognizer={}, offendingSymbol={}, line={}, charPositionInLine={}, msg={}",
						recognizer, offendingSymbol, line, charPositionInLine, msg);
			}

			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					int prediction, ATNConfigSet configs) {
				logger.error(
						"Context Sensitivity: recognizer={}  dfa={} startIndex={}, stopIndex={} prediction={} configs={}",
						recognizer, dfa, startIndex, stopIndex, prediction, configs);
			}

			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {
				logger.error(
						"Attempting Full Context: recognizer={}  dfa={} startIndex={}, stopIndex={} conflictingAlts={} configs={}",
						recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs);
			}

			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {
				logger.error("Ambiguity: recognizer={}  dfa={} startIndex={}, stopIndex={} ambigAlts={} configs={}",
						recognizer, dfa, startIndex, stopIndex, ambigAlts, configs);
			}
		});
		ParseTreeWalker walker = new ParseTreeWalker();
		SuiteCreator creator = new SuiteCreator();
		walker.walk(creator, parser.suite());
		return creator.getTestSuite();
	}
}
