package io.vepo.plaintest;

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
	private static class Tuple<K, V> {
		private K key;
		private V value;

		Tuple(K key, V value) {
			this.key = key;
			this.value = value;
		}

	}

	private static <K, V> Tuple<K, V> entry(K key, V value) {
		return new Tuple<K, V>(key, value);
	}

	private static <K, V> Map<K, V> ofEntries(Tuple<K, V>... tuples) {
		return Stream.of(tuples).collect(toMap(entry -> entry.key, entry -> entry.value));
	}

	@Nested
	public class ExecutionDirectoryTest {
		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory absolute Unix Path")
		public void absoluteUnixPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: /opt/xyz/qasd\n" + "}"), new Suite(0, "T1",
					asList(), asList(), ofEntries(entry(SuiteAttributes.EXECUTION_PATH, "/opt/xyz/qasd"))));
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory absolute Windows Path")
		public void absoluteWindowsPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: C:\\user\\xyx\n" + "}"), new Suite(0, "T1",
					asList(), asList(), ofEntries(entry(SuiteAttributes.EXECUTION_PATH, "C:\\user\\xyx"))));
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory relative Unix Path")
		public void relativeUnixPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: ./src/main/java\n" + "}"), new Suite(0, "T1",
					asList(), asList(), ofEntries(entry(SuiteAttributes.EXECUTION_PATH, "./src/main/java"))));
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory relative Windows Path")
		public void relativeWindowsPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: .\\src\\main\\java\n" + "}"), new Suite(0, "T1",
					asList(), asList(), ofEntries(entry(SuiteAttributes.EXECUTION_PATH, ".\\src\\main\\java"))));
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory  Unix Path")
		public void unixPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: src/main/java\n" + "}"), new Suite(0, "T1",
					asList(), asList(), ofEntries(entry(SuiteAttributes.EXECUTION_PATH, "src/main/java"))));
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory Windows Path")
		public void windowsPathTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: src\\main\\java\n" + "}"), new Suite(0, "T1",
					asList(), asList(), ofEntries(entry(SuiteAttributes.EXECUTION_PATH, "src\\main\\java"))));
		}

		@Test
		@DisplayName("It SHOULD parse a Suite with execution directory simple filename")
		public void simpleTest() {
			assertEquals(parseSuite("Suite T1 {\n" + "    exec-dir: src\n" + "}"),
					new Suite(0, "T1", asList(), asList(), ofEntries(entry(SuiteAttributes.EXECUTION_PATH, "src"))));
		}

	}

	@Test
	@DisplayName("It SHOULD parse an empty Suite")
	public void emptySuiteParseTest() {
		assertEquals(parseSuite("Suite T1 { }"), new Suite(0, "T1", asList(), asList(), ofEntries()));
	}

	@Test
	@DisplayName("It SHOULD parse index of inner Steps")
	public void indexTest() {
		assertEquals(parseSuite("Suite T1 {\n" + //
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
				"}"), new Suite(0, "T1", asList(new Suite(3, "T2", asList(),
						asList(new Step(0, "CMD", "Step2.1", ofEntries(entry("cmd", "ls")), asList())), ofEntries())),
						asList(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "GET")),
								asList(new Assertion<>("responseCode", "Equals", 200L))),
								new Step(1, "CMD", "Step2", ofEntries(entry("cmd", "ls")), asList()),
								new Step(2, "CMD", "Step3", ofEntries(entry("cmd", "cd new-folder")), asList()),
								new Step(4, "CMD", "Step4", ofEntries(entry("cmd", "ls *.txt")), asList())),
						ofEntries()));
	}

	@Test
	@DisplayName("It SHOULD parse Suite with inner Suites")
	public void innerSuiteTest() {
		assertEquals(parseSuite("Suite T1 {\n" + //
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
				"}\n"), new Suite(0, "T1", asList(new Suite(1, "T2",
						asList(new Suite(1, "T3", asList(),
								asList(new Step(0, "HTTP", "Step3", ofEntries(entry("method", "PUT")), asList())),
								ofEntries())),
						asList(new Step(0, "HTTP", "Step2", ofEntries(entry("method", "POST")), asList())),
						ofEntries())),
						asList(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "GET")), asList())),
						ofEntries()));

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
				"}"), new Suite(0, "T1", asList(),
						asList(new Step(0, "HTTP", "Step1", ofEntries(entry("method", "POST"), entry("body", "{\n" + //
								"    \"id\": 1,\n" + //
								"    \"username\": \"vepo\"\n" + //
								"}")), asList(new Assertion<>("responseCode", "Equals", 200L)))),
						ofEntries()));
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
				new Suite(0, "T1", asList(),
						asList(new Step(0, "HTTP", "Step1",
								ofEntries(entry("method", "GET"), entry("timeout", 1000L), entry("minValue", -5L),
										entry("maxValue", 1500000000L)),
								asList(new Assertion<>("responseCode", "Equals", 200L),
										new Assertion<>("responseMessage", "Equals", "OK")))),
						ofEntries()));
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
				"}"),
				new Suite(
						0, "T1", asList(), asList(
								new Step(0, "HTTP", "Step1",
										ofEntries(entry("method", "GET"), entry("timeout", 1000L),
												entry("minValue", -5L), entry("maxValue", 1500000000L)),
										asList())),
						ofEntries()));
	}
}
