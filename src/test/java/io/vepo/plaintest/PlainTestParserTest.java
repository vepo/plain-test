package io.vepo.plaintest;

import static io.vepo.plaintest.parser.TestSuiteFactory.parseSuite;
import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PlainTestParserTest {
	@Test
	public void emptySuiteParseTest() {
		assertEquals(parseSuite("Suite T1 { }"), new Suite("T1", asList(), asList()));
	}

	@Test
	public void suiteWithPluginParseTest() {
		assertEquals(parseSuite("""
				Suite T1 {
					HTTP Step1 {
						method: "GET"
						timeout: 1000
						minValue: -5
						maxValue: 1500000000
					}
				}
				"""), new Suite("T1", asList(), asList(new Step("HTTP", "Step1", ofEntries(entry("method", "GET"),
				entry("timeout", 1000L), entry("minValue", -5L), entry("maxValue", 1500000000L)), ofEntries()))));
	}

	@Test
	public void innerSuiteTest() {
		assertEquals(parseSuite("""
				Suite T1 {
					HTTP Step1 {
						method: "GET"
					}
					Suite T2 {
						HTTP Step2 {
							method: "POST"
						}
						Suite T3 {
							HTTP Step3 {
								method: "PUT"
							}
						}
					}
				}
				"""),
				new Suite("T1",
						asList(new Suite("T2", asList(new Suite("T3", asList(),
								asList(new Step("HTTP", "Step3", ofEntries(entry("method", "PUT")), ofEntries())))),
								asList(new Step("HTTP", "Step2", ofEntries(entry("method", "POST")), ofEntries())))),
						asList(new Step("HTTP", "Step1", ofEntries(entry("method", "GET")), ofEntries()))));

	}

	@Test
	void multilineStringTest() {
		assertEquals(parseSuite("""
				Suite T1 {
					HTTP Step1 {
						method  : "POST"
						body    : \"\"\"
						          {
						              "id": 1,
						              "username": "vepo"
						          }
						          \"\"\"
						assert responseCode   : 200
					}
				}
				"""), new Suite("T1", asList(),
				asList(new Step("HTTP", "Step1", ofEntries(entry("method", "POST"), entry("body", """
						{
						    "id": 1,
						    "username": "vepo"
						}""")), ofEntries(entry("responseCode", 200L))))));
	}

	@Test
	void testStepWithAssertion() {
		assertEquals(parseSuite("""
				Suite T1 {
					HTTP Step1 {
						method  : "GET"
						timeout : 1000
						minValue: -5
						maxValue: 1500000000
						assert responseCode   : 200
						assert responseMessage: "OK"
					}
				}
				"""),
				new Suite("T1", asList(),
						asList(new Step("HTTP", "Step1",
								ofEntries(entry("method", "GET"), entry("timeout", 1000L), entry("minValue", -5L),
										entry("maxValue", 1500000000L)),
								ofEntries(entry("responseCode", 200L), entry("responseMessage", "OK"))))));
	}
}
