package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SuiteParserTest {

	@Nested
	public class ExecutionDirectoryTest {
		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory absolute Unix Path")
		public void absoluteUnixPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: /opt/xyz/qasd\n" + "}"),
					Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "/opt/xyz/qasd").build());
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory absolute Windows Path")
		public void absoluteWindowsPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + //
					"    exec-dir: C:\\user\\xyx\n" + //
					"}"), Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "C:\\user\\xyx").build());
			;
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory relative Unix Path")
		public void relativeUnixPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + //
					"    exec-dir: ./src/main/java\n" + //
					"}"), Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "./src/main/java").build());
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory relative Windows Path")
		public void relativeWindowsPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: .\\src\\main\\java\n" + "}"),
					Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, ".\\src\\main\\java").build());
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory  Unix Path")
		public void unixPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: src/main/java\n" + "}"),
					Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "src/main/java").build());
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory Windows Path")
		public void windowsPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: src\\main\\java\n" + "}"),
					Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "src\\main\\java").build());
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory simple filename")
		public void simpleTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: src\n" + "}"),
					Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "src").build());
		}

	}

	@Test
	@DisplayName("It SHOULD parse an empty Suite")
	public void emptySuiteParseTest() {
		assertEquals(parseSuite("Suite T1 { }"), Suite.builder().index(0).name("T1").build());
	}

	@Test
	@DisplayName("It SHOULD parse index of inner Steps")
	public void indexTest() {
		assertEquals(Suite.builder().index(0).name("T1")
				.child(Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
						.assertion(new Assertion<>("responseCode", "Equals", 200L)).build())
				.child(Step.builder().index(1).plugin("Process").name("Step2").attribute("cmd", "ls").build())
				.child(Step.builder().index(2).plugin("Process").name("Step3").attribute("cmd", "cd new-folder")
						.build())
				.child(Suite.builder().index(3).name("T2")
						.child(Step.builder().index(0).plugin("Process").name("Step2.1").attribute("cmd", "ls").build())
						.build())
				.child(Step.builder().index(4).plugin("Process").name("Step4").attribute("cmd", "ls *.txt").build())
				.build(), parseSuite("Suite T1 {\n" + //
						"    HTTP Step1 {\n" + //
						"        method  : \"GET\"\n" + //
						"        assert responseCode Equals 200\n" + //
						"    }\n" + //
						"    Process Step2 {\n" + //
						"        cmd: \"ls\"\n" + //
						"    }\n" + //
						"    Process Step3 {\n" + //
						"        cmd: \"cd new-folder\"\n" + //
						"    }\n" + //
						"    Suite T2 {\n" + //
						"        Process Step2.1 {\n" + //
						"            cmd: \"ls\"\n" + //
						"        }\n" + //
						"    }\n" + //
						"    Process Step4 {\n" + //
						"        cmd: \"ls *.txt\"\n" + //
						"    }\n" + //
						"}"));

	}

	@Test
	@DisplayName("It SHOULD parse Suite with inner Suites")
	public void innerSuiteTest() {
		assertEquals(
				Suite.builder().index(0).name("T1")
						.child(Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET").build())
						.child(Suite.builder().index(1).name("T2")
								.child(Step.builder().index(0).plugin("HTTP").name("Step2").attribute("method", "POST")
										.build())
								.child(Suite.builder().index(1).name("T3")
										.child(Step.builder().index(0).plugin("HTTP").name("Step3")
												.attribute("method", "PUT").build())
										.build())
								.build())
						.build(),
				parseSuite("Suite T1 {\n" + //
						"    HTTP Step1 {\n" + //
						"        method: \"GET\"\n" + //
						"    }\n" + //
						"    Suite T2 {\n" + //
						"        HTTP Step2 {\n" + //
						"            method: \"POST\"\n" + //
						"        }\n" + //
						"        Suite T3 {\n" + //
						"            HTTP Step3 {\n" + //
						"                method: \"PUT\"\n" + //
						"            }\n" + //
						"        }\n" + //
						"    }\n" + //
						"}\n"));
	}

	@Test
	@DisplayName("It SHOULD parse parameter value Multi Line String")
	public void multilineStringTest() {
		assertEquals(Suite.builder().index(0).name("T1").child(Step.builder().index(0).plugin("HTTP").name("Step1")
				.attribute("method", "POST").attribute("body", "{\n" + //
						"    \"id\": 1,\n" + //
						"    \"username\": \"vepo\"\n" + //
						"}")
				.assertion(new Assertion<>("responseCode", "Equals", 200L)).build()).build(),
				parseSuite("Suite T1 {\n" + //
						"    HTTP Step1 {\n" + //
						"        method  : \"POST\"\n" + //
						"        body    : \"\"\"\n" + //
						"                  {\n" + //
						"                      \"id\": 1,\n" + //
						"                      \"username\": \"vepo\"\n" + //
						"                  }\n" + //
						"                  \"\"\"\n" + //
						"        assert responseCode Equals 200\n" + //
						"    }\n" + //
						"}"));
	}

	@Test
	@DisplayName("It SHOULD parse Step assertion")
	public void stepWithAssertionTest() {
		assertEquals(Suite.builder().index(0).name("T1")
				.child(Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
						.attribute("timeout", 1000L).attribute("minValue", -5L).attribute("maxValue", 1500000000L)

						.assertion(new Assertion<>("responseCode", "Equals", 200L))
						.assertion(new Assertion<>("responseMessage", "Equals", "OK"))
						.assertion(new Assertion<>("body", "Equals", null)).build())
				.build(), parseSuite("Suite T1 {\n" + //
						"    HTTP Step1 {\n" + //
						"        method  : \"GET\"\n" + //
						"        timeout : 1000\n" + //
						"        minValue: -5\n" + //
						"        maxValue: 1500000000\n" + //
						"        assert responseCode    Equals 200\n" + //
						"        assert responseMessage Equals \"OK\"\n" + //
						"        assert body Equals null\n" + //
						"    }\n" + //
						"}"));
	}

	@Test
	@DisplayName("It SHOULD parse a Suite with a Plugin")
	public void suiteWithPluginParseTest() {
		assertEquals(Suite.builder().index(0).name("T1")
				.child(Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
						.attribute("timeout", 1000L).attribute("minValue", -5L).attribute("maxValue", 1500000000L)
						.build())
				.build(), parseSuite("Suite T1 {\n" + //
						"    HTTP Step1 {\n" + //
						"        method: \"GET\"\n" + //
						"        timeout: 1000\n" + //
						"        minValue: -5\n" + //
						"        maxValue: 1500000000\n" + //
						"    }\n" + //
						"}"));
	}

	@Nested
	@DisplayName("Properties")
	public class PropertiesTest {
		@Test
		@DisplayName("It should allow Properties")
		public void parsePropertiesTest() {
			assertEquals(
					Suite.builder().index(0).name("T1")
							.child(Properties.builder().index(0).value("command", "ls").value("timeout", 150L).build())
							.child(Step.builder().index(1).name("Step1").plugin("CMD")
									.attribute("cmd", new PropertyReference("command"))
									.attribute("timeout", new PropertyReference("timeout")).build())
							.build(),
					parseSuite("Suite T1 {\n" + //
							"    Properties {\n" + //
							"        command: \"ls\"\n" + //
							"        timeout: 150\n" + //
							"    }\n" + //
							"    CMD Step1 {\n" + //
							"        cmd: ${command}\n" + //
							"        timeout: ${timeout}\n" + //
							"    }\n" + //
							"}"));
		}

		@Test
		@DisplayName("It should allow Properties in inner Suites")
		public void parsePropertiesInnerSuiteTest() {
			assertEquals(
					Suite.builder().index(0).name("T1")
							.child(Properties.builder().index(0).value("command", "ls").value("timeout", 150L).build())
							.child(Step.builder().index(1).name("Step1").plugin("CMD")
									.attribute("cmd", new PropertyReference("command"))
									.attribute("timeout", new PropertyReference("timeout")).build())
							.child(Suite.builder().index(2).name("T2")
									.child(Properties.builder().index(0).value("command2", "dir").value("timeout2", 50L)
											.build())
									.child(Step.builder().index(1).name("Step2").plugin("CMD")
											.attribute("cmd", new PropertyReference("command2"))
											.attribute("timeout", new PropertyReference("timeout2")).build())
									.build())
							.build(),
					parseSuite("Suite T1 {\n" + //
							"    Properties {\n" + //
							"        command: \"ls\"\n" + //
							"        timeout: 150\n" + //
							"    }\n" + //
							"    CMD Step1 {\n" + //
							"        cmd: ${command}\n" + //
							"        timeout: ${timeout}\n" + //
							"    }\n" + //
							"    Suite T2 {\n" + //
							"        Properties {\n" + //
							"            command2: \"dir\"\n" + //
							"            timeout2: 50\n" + //
							"        }\n" + //
							"        CMD Step2 {\n" + //
							"            cmd: ${command2}\n" + //
							"            timeout: ${timeout2}\n" + //
							"        }\n" + //
							"    }" + //
							"}"));
		}
	}
}
