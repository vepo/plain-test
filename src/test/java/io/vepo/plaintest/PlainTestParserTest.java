package io.vepo.plaintest;

import static io.vepo.plaintest.parser.TestSuiteFactory.parseTestSuite;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PlainTestParserTest {
	@Test
	public void emptyTestSuiteParseTest() {
		assertEquals(parseTestSuite("TestSuite T1 { }").name(), "T1");
	}

	@Test
	public void testSuiteWithPluginParseTest() {
		var testSuite = parseTestSuite("""
				TestSuite T1 {
					HTTP Step1 {
						method: "GET"
						timeout: 1000
						minValue: -5
						maxValue: 1500000000
					}
				}
				""");
		assertEquals("T1", testSuite.name());
		assertEquals(1, testSuite.steps().size());

		var testStep = testSuite.steps().get(0);

		assertEquals("HTTP", testStep.plugin());
		assertEquals("Step1", testStep.name());
		assertThat(testStep.attributes()).containsOnly(entry("method", "GET"), entry("timeout", 1000L),
				entry("minValue", -5L), entry("maxValue", 1500000000L));

	}
}
