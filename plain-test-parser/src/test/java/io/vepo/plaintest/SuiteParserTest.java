package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SuiteParserTest {
	private static class Tuple {
		private String key;
		private Object value;

		Tuple(String key, Object value) {
			this.key = key;
			this.value = value;
		}

	}

	private static Tuple entry(String key, Object value) {
		return new Tuple(key, value);
	}

	private static Map<String, Object> ofEntries(Tuple... tuples) {
		return Stream.of(tuples).collect(toMap(entry -> entry.key, entry -> entry.value));
	}

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
		assertEquals(
				Suite.builder().index(0).name("T1")
						.step(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "GET")),
								asList(new Assertion<>("responseCode", "Equals", 200L))))
						.step(new Step(1, "CMD", "Step2", ofEntries(entry("cmd", "ls")), asList()))
						.step(new Step(2, "CMD", "Step3", ofEntries(entry("cmd", "cd new-folder")), asList()))
						.suite(Suite.builder().index(3).name("T2")
								.step(new Step(0, "CMD", "Step2.1", ofEntries(entry("cmd", "ls")), asList())).build())
						.step(new Step(4, "CMD", "Step4", ofEntries(entry("cmd", "ls *.txt")), asList())).build(),
				parseSuite("Suite T1 {\n" + //
						"    HTTP Step1 {\n" + //
						"        method  : \"GET\"\n" + //
						"        assert responseCode Equals 200\n" + //
						"    }\n" + //
						"    CMD Step2 {\n" + //
						"        cmd: \"ls\"\n" + //
						"    }\n" + //
						"    CMD Step3 {\n" + //
						"        cmd: \"cd new-folder\"\n" + //
						"    }\n" + //
						"    Suite T2 {\n" + //
						"        CMD Step2.1 {\n" + //
						"            cmd: \"ls\"\n" + //
						"        }\n" + //
						"    }\n" + //
						"    CMD Step4 {\n" + //
						"        cmd: \"ls *.txt\"\n" + //
						"    }\n" + //
						"}"));

	}

	@Test
	@DisplayName("It SHOULD parse Suite with inner Suites")
	public void innerSuiteTest() {
		assertEquals(Suite.builder().index(0).name("T1")
				.step(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "GET")), asList()))
				.suite(Suite.builder().index(1).name("T2")
						.step(new Step(0, "HTTP", "Step2", ofEntries(entry("method", "POST")), asList()))
						.suite(Suite.builder().index(1).name("T3")
								.step(new Step(0, "HTTP", "Step3", ofEntries(entry("method", "PUT")), asList()))
								.build())
						.build())
				.build(), parseSuite("Suite T1 {\n" + //
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
		assertEquals(parseSuite("Suite T1 {\n" + //
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
				"}"), Suite.builder().index(0).name("T1")
						.step(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "POST"), entry("body", "{\n" + //
								"    \"id\": 1,\n" + //
								"    \"username\": \"vepo\"\n" + //
								"}")), asList(new Assertion<>("responseCode", "Equals", 200L))))
						.build());
	}

	@Test
	@DisplayName("It SHOULD parse Step assertion")
	public void stepWithAssertionTest() {
		assertEquals(parseSuite("Suite T1 {\n" + //
				"    HTTP Step1 {\n" + //
				"        method  : \"GET\"\n" + //
				"        timeout : 1000\n" + //
				"        minValue: -5\n" + //
				"        maxValue: 1500000000\n" + //
				"        assert responseCode    Equals 200\n" + //
				"        assert responseMessage Equals \"OK\"\n" + //
				"    }\n" + //
				"}"),
				Suite.builder().index(0).name("T1")
						.step(new Step(0, "HTTP", "Step1",
								ofEntries(entry("method", "GET"), entry("timeout", 1000L), entry("minValue", -5L),
										entry("maxValue", 1500000000L)),
								asList(new Assertion<>("responseCode", "Equals", 200L),
										new Assertion<>("responseMessage", "Equals", "OK"))))
						.build());
	}

	@Test
	@DisplayName("It SHOULD parse a Suite with a Plugin")
	public void suiteWithPluginParseTest() {
		assertEquals(parseSuite("Suite T1 {\n" + //
				"    HTTP Step1 {\n" + //
				"        method: \"GET\"\n" + //
				"        timeout: 1000\n" + //
				"        minValue: -5\n" + //
				"        maxValue: 1500000000\n" + //
				"    }\n" + //
				"}"), Suite
						.builder().index(0).name("T1").step(
								new Step(0, "HTTP", "Step1",
										ofEntries(entry("method", "GET"), entry("timeout", 1000L),
												entry("minValue", -5L), entry("maxValue", 1500000000L)),
										asList()))
						.build());
	}
}
