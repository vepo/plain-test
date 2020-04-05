package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SuiteParserTest {
	@Test
	@DisplayName("It SHOULD parse an empty Suite")
	public void emptySuiteParseTest() {
		assertEquals(parseSuite("Suite T1 { }"), new Suite(0, "T1", asList(), asList()));
	}

	@Test
	@DisplayName("It SHOULD parse a Suite with a Plugin")
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
				"""), new Suite(0, "T1", asList(), asList(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "GET"),
				entry("timeout", 1000L), entry("minValue", -5L), entry("maxValue", 1500000000L)), ofEntries()))));
	}

	@Test
	@DisplayName("It SHOULD parse Suite with inner Suites")
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
				new Suite(0, "T1",
						asList(new Suite(1, "T2", asList(new Suite(1, "T3", asList(),
								asList(new Step(0, "HTTP", "Step3", ofEntries(entry("method", "PUT")), ofEntries())))),
								asList(new Step(0, "HTTP", "Step2", ofEntries(entry("method", "POST")), ofEntries())))),
						asList(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "GET")), ofEntries()))));

	}

	@Test
	@DisplayName("It SHOULD parse parameter value Multi Line String")
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
				"""), new Suite(0, "T1", asList(),
				asList(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "POST"), entry("body", """
						{
						    "id": 1,
						    "username": "vepo"
						}""")), ofEntries(entry("responseCode", 200L))))));
	}

	@Test
	@DisplayName("It SHOULD parse Step assertion")
	void stepWithAssertionTest() {
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
				new Suite(0, "T1", asList(),
						asList(new Step(0, "HTTP", "Step1",
								ofEntries(entry("method", "GET"), entry("timeout", 1000L), entry("minValue", -5L),
										entry("maxValue", 1500000000L)),
								ofEntries(entry("responseCode", 200L), entry("responseMessage", "OK"))))));
	}

	@Test
	@DisplayName("It SHOULD parse index of inner Steps")
	void indexTest() {
		assertEquals(parseSuite("""
				Suite T1 {
					HTTP Step1 {
						method  : "GET"
						assert responseCode: 200
					}
					CMD Step2 {
						cmd: "ls"
					}
					CMD Step3 {
						cmd: "cd new-folder"
					}
					Suite T2 {
						CMD Step2.1 {
							cmd: "ls"
						}
					}
					CMD Step4 {
						cmd: "ls *.txt"
					}
				}
				"""),
				new Suite(0, "T1",
						asList(new Suite(3, "T2", asList(),
								asList(new Step(0, "CMD", "Step2.1", ofEntries(entry("cmd", "ls")), ofEntries())))),
						asList(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "GET")),
								ofEntries(entry("responseCode", 200L))),
								new Step(1, "CMD", "Step2", ofEntries(entry("cmd", "ls")), ofEntries()),
								new Step(2, "CMD", "Step3", ofEntries(entry("cmd", "cd new-folder")), ofEntries()),
								new Step(4, "CMD", "Step4", ofEntries(entry("cmd", "ls *.txt")), ofEntries()))));
	}
}
