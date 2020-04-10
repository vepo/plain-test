package io.vepo.plaintest.runner.executor;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.vepo.plaintest.Suite;
import io.vepo.plaintest.SuiteFactory;
import io.vepo.plaintest.runner.utils.Os;
import io.vepo.plaintest.runner.utils.Os.OS;

public class BashCommandTest extends AbstractTest {

	private static final String BASH_SUCCESS_TEST = Os.getOS() == OS.WINDOWS ? "Suite BashTest {\n" + //
			"    exec-dir: src\n" + //
			"\n" + //
			"    CMD EnterDir {\n" + //
			"        cmd    : \"dir\"\n" + //
			"        timeout: 500\n" + //
			"        assert stdout Contains \"main\"\n" + //
			"    }\n" + //
			"\n" + //
			"    Suite MainTest {\n" + //
			"        exec-dir: main\n" + //
			"        CMD EnterSubFolder {\n" + //
			"            cmd    : \"dir\"\n" + //
			"            timeout: 500\n" + //
			"            assert stdout Contains \"java\"\n" + //
			"        }\n" + //
			"    }\n" + //
			"}" : "Suite BashTest {\n" + //
					"    exec-dir: src\n" + //
					"\n" + //
					"    CMD EnterDir {\n" + //
					"        cmd    : \"ls\"\n" + //
					"        timeout: 500\n" + //
					"        assert stdout Contains \"main\"\n" + //
					"    }\n" + //
					"\n" + //
					"    Suite MainTest {\n" + //
					"        exec-dir: main\n" + //
					"        CMD EnterSubFolder {\n" + //
					"            cmd    : \"ls\"\n" + //
					"            timeout: 500\n" + //
					"            assert stdout Contains \"java\"\n" + //
					"        }\n" + //
					"    }\n" + //
					"}";

	private static final String BASH_FAIL_TEST = "Suite BashTest {\n" + //
			"\n" + //
			"    CMD Error {\n" + //
			"        cmd    : \"commandThatDoesNotExists\"\n" + //
			"        timeout: 500\n" + //
			"    }\n" + //
			"}";

	private static final String BASH_FAIL_ASSERTION = Os.getOS() == OS.WINDOWS ? "Suite BashTest {\n" + //
			"\n" + //
			"CMD Error {\n" + //
			"    cmd    : \"dir\"\n" + //
			"    timeout: 500\n" + //
			"    assert stdout Contains \"String That DOES NOT EXIST!\"\n" + //
			"}\n" + //
			"}" : "Suite BashTest {\n" + //
					"\n" + //
					"    CMD Error {\n" + //
					"        cmd    : \"ls\"\n" + //
					"        timeout: 500\n" + //
					"        assert stdout Contains \"String That DOES NOT EXIST!\"\n" + //
					"    }\n" + //
					"}";

	private static final String BASH_ASSERTION_STRING_SUCCESS_EQUALS = "Suite EchoTest {\n" + //
			"\n" + //
			"    CMD EchoSomeString {\n" + //
			"        cmd    : \"echo some string\"\n" + //
			"        timeout: 500\n" + //
			"        assert stdout Equals \"some string\"\n" + //
			"    }\n" + //
			"}";

	private static final String BASH_ASSERTION_STRING_FAILED_EQUALS = "Suite EchoTest {\n" + //
			"\n" + //
			"    CMD EchoSomeString {\n" + //
			"        cmd    : \"echo some string\"\n" + //
			"        timeout: 500\n" + //
			"        assert stdout Equals \"string\"\n" + //
			"    }\n" + //
			"}";

	private static final String BASH_ASSERTION_MISSING_ATTRIBUTE = "Suite EchoTest {\n" + //
			"\n" + //
			"    CMD EchoSomeString {\n" + //
			"        timeout: 500\n" + //
			"        assert stdout Equals \"other string\"\n" + //
			"    }\n" + //
			"}";

	@Test
	public void listCommandTest() {
		Suite suite = SuiteFactory.parseSuite(BASH_SUCCESS_TEST);
		PlainTestExecutor executor = new PlainTestExecutor();
		assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
				.satisfies(result -> assertThat(this.find(result, "EnterDir")).isPresent().get()
						.satisfies(r -> assertTrue(r.isSuccess())))
				.satisfies(result -> assertThat(this.find(result, "MainTest")).isPresent().get()
						.satisfies(r -> assertTrue(r.isSuccess())));
	}

	@Test
	public void failCommandTest() {
		Suite suite = SuiteFactory.parseSuite(BASH_FAIL_TEST);
		PlainTestExecutor executor = new PlainTestExecutor();
		assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
				.satisfies(result -> assertThat(this.find(result, "Error")).isPresent().get()
						.satisfies(r -> assertFalse(r.isSuccess())));
	}

	@Test
	public void failAssertionCommandTest() {
		Suite suite = SuiteFactory.parseSuite(BASH_FAIL_ASSERTION);
		PlainTestExecutor executor = new PlainTestExecutor();
		assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
				.satisfies(result -> assertThat(this.find(result, "Error")).isPresent().get()
						.satisfies(r -> assertFalse(r.isSuccess())));
	}

	@Nested
	public class MissingAttributeTest {
		@Test
		public void stringSuccessTest() {
			Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_MISSING_ATTRIBUTE);
			PlainTestExecutor executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(BashCommandTest.this.find(result, "EchoSomeString")).isPresent()
							.get().satisfies(r -> assertFalse(r.isSuccess()))
							.satisfies(r -> assertEquals(
									asList(new Fail(FailReason.MISSING_ATTRIBUTES, "Missing attributes: [cmd]")),
									r.getFails())));
		}
	}

	@Nested
	public class AssertContainsTest {

		private static final String BASH_ASSERTION_STRING_SUCCESS_CONTAINS = "Suite EchoTest {\n" + //
				"\n" + //
				"    CMD EchoSomeString {\n" + //
				"        cmd    : \"echo some string\"\n" + //
				"        timeout: 500\n" + //
				"        assert stdout Equals \"some string\"\n" + //
				"    }\n" + //
				"}";

		@Test
		public void stringSuccessTest() {
			Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_SUCCESS_CONTAINS);
			PlainTestExecutor executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(BashCommandTest.this.find(result, "EchoSomeString")).isPresent()
							.get().satisfies(r -> assertTrue(r.isSuccess())));
		}

		private static final String BASH_ASSERTION_STRING_FAILED_CONTAINS = "Suite EchoTest {\n" + //
				"\n" + //
				"    CMD EchoSomeString {\n" + //
				"        cmd    : \"echo some string\"\n" + //
				"        timeout: 500\n" + //
				"        assert stdout Equals \"other string\"\n" + //
				"    }\n" + //
				"}";

		@Test
		public void stringFailedTest() {
			Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_FAILED_CONTAINS);
			PlainTestExecutor executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(BashCommandTest.this.find(result, "EchoSomeString")).isPresent()
							.get().satisfies(r -> assertFalse(r.isSuccess())));
		}
	}

	@Disabled
	@Nested
	public class AssertEqualsTest {

		@Test
		public void stringSuccessTest() {
			Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_SUCCESS_EQUALS);
			PlainTestExecutor executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(BashCommandTest.this.find(result, "EchoSomeString")).isPresent()
							.get().satisfies(r -> assertTrue(r.isSuccess())));
		}

		@Test
		public void stringFailedTest() {
			Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_FAILED_EQUALS);
			PlainTestExecutor executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(BashCommandTest.this.find(result, "EchoSomeString")).isPresent()
							.get().satisfies(r -> assertFalse(r.isSuccess())));
		}
	}
}
