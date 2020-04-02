package io.vepo.plaintest;

import static io.vepo.plaintest.parser.TestSuiteFactory.parseTestSuite;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PlainTestParserTest {
	@Test
	public void emptyTestSuiteParseTest() {
		assertEquals(parseTestSuite("TestSuite T1 { }").getName(), "T1");
	}
}
