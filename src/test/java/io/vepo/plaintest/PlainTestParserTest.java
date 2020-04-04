package io.vepo.plaintest;

import static io.vepo.plaintest.parser.TestSuiteFactory.parseTestSuite;
import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.assertj.core.util.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class PlainTestParserTest {
	@Test
	public void emptyTestSuiteParseTest() {
		assertEquals(parseTestSuite("TestSuite T1 { }"), new TestSuite("T1", asList(), asList()));
	}

	@Test
	public void testSuiteWithPluginParseTest() {
		assertEquals(parseTestSuite("""
					TestSuite T1 {
					HTTP Step1 {
						method: "GET"
						timeout: 1000
						minValue: -5
						maxValue: 1500000000
					}
				}
				"""),
				new TestSuite("T1", asList(), asList(new TestStep("HTTP", "Step1", Map.ofEntries(entry("method", "GET"),
						entry("timeout", 1000L), entry("minValue", -5L), entry("maxValue", 1500000000L))))));
	}

	@Test
	public void innerTestSuiteTest() {
		assertEquals(parseTestSuite("""
				TestSuite T1 {
					HTTP Step1 {
						method: "GET"
					}
					TestSuite T2 {
						HTTP Step2 {
							method: "POST"
						}
						TestSuite T3 {
							HTTP Step3 {
								method: "PUT"
							}
						}
					}
				}
				"""),
				new TestSuite("T1",
						asList(new TestSuite("T2",
								asList(new TestSuite("T3", asList(),
										asList(new TestStep("HTTP", "Step3", newHashMap("method", "PUT"))))),
								asList(new TestStep("HTTP", "Step2", newHashMap("method", "POST"))))),
						asList(new TestStep("HTTP", "Step1", newHashMap("method", "GET")))));

	}
}
