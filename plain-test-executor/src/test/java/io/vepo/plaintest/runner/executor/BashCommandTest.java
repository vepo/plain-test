package io.vepo.plaintest.runner.executor;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.vepo.plaintest.SuiteFactory;
import io.vepo.plaintest.runner.utils.Os;
import io.vepo.plaintest.runner.utils.Os.OS;

public class BashCommandTest {

	private static final String BASH_SUCCESS_TEST = Os.getOS() == OS.WINDOWS ? """
			Suite BashTest {
			    exec-dir: src

			    CMD EnterDir {
			        cmd    : "dir"
			        timeout: 500
			        assert stdout Contains "src"
			    }

			    Suite MainTest {
			        exec-dir: main
			        CMD EnterSubFolder {
			            cmd    : "dir"
			            timeout: 500
			            assert stdout Contains "java"
			        }
			    }
			}""" : """
			Suite BashTest {
			    exec-dir: src

			    CMD EnterDir {
			        cmd    : "ls"
			        timeout: 500
			        assert stdout Contains "src"
			    }

			    Suite MainTest {
			        exec-dir: main
			        CMD EnterSubFolder {
			            cmd    : "ls"
			            timeout: 500
			            assert stdout Contains "java"
			        }
			    }
			}""";

	private static final String BASH_FAIL_TEST = """
			Suite BashTest {

			    CMD Error {
			        cmd    : "commandThatDoesNotExists"
			        timeout: 500
			    }
			}""";

	private static final String BASH_FAIL_ASSERTION = Os.getOS() == OS.WINDOWS ? """
				Suite BashTest {

			    CMD Error {
			        cmd    : "dir"
			        timeout: 500
			        assert stdout Contains "String That DOES NOT EXIST!"
			    }
			}""" : """
			Suite BashTest {

			    CMD Error {
			        cmd    : "ls"
			        timeout: 500
			        assert stdout Contains "String That DOES NOT EXIST!"
			    }
			}""";

	private static final String BASH_ASSERTION_STRING_SUCCESS_EQUALS = """
			Suite EchoTest {

			    CMD EchoSomeString {
			        cmd    : "echo some string"
			        timeout: 500
			        assert stdout Equals "some string"
			    }
			}
			""";

	private static final String BASH_ASSERTION_STRING_FAILED_EQUALS = """
			Suite EchoTest {

			    CMD EchoSomeString {
			        cmd    : "echo some string"
			        timeout: 500
			        assert stdout Equals "string"
			    }
			}
			""";

	private static final String BASH_ASSERTION_STRING_SUCCESS_CONTAINS = """
			Suite EchoTest {

			    CMD EchoSomeString {
			        cmd    : "echo some string"
			        timeout: 500
			        assert stdout Equals "some string"
			    }
			}
			""";

	private static final String BASH_ASSERTION_STRING_FAILED_CONTAINS = """
			Suite EchoTest {

			    CMD EchoSomeString {
			        cmd    : "echo some string"
			        timeout: 500
			        assert stdout Equals "other string"
			    }
			}
			""";

	private static final String BASH_ASSERTION_MISSING_ATTRIBUTE = """
			Suite EchoTest {

			    CMD EchoSomeString {
			        timeout: 500
			        assert stdout Equals "other string"
			    }
			}
			""";

	@Test
	public void listCommandTest() {
		var suite = SuiteFactory.parseSuite(BASH_SUCCESS_TEST);
		var executor = new PlainTestExecutor();
		assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.success()))
				.satisfies(result -> assertThat(find(result, "EnterDir")).isPresent().get()
						.satisfies(r -> assertTrue(r.success())))
				.satisfies(result -> assertThat(find(result, "MainTest")).isPresent().get()
						.satisfies(r -> assertTrue(r.success())));
	}

	@Test
	public void failCommandTest() {
		var suite = SuiteFactory.parseSuite(BASH_FAIL_TEST);
		var executor = new PlainTestExecutor();
		assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.success())).satisfies(
				result -> assertThat(find(result, "Error")).isPresent().get().satisfies(r -> assertFalse(r.success())));
	}

	@Test
	public void failAssertionCommandTest() {
		var suite = SuiteFactory.parseSuite(BASH_FAIL_ASSERTION);
		var executor = new PlainTestExecutor();
		assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.success())).satisfies(
				result -> assertThat(find(result, "Error")).isPresent().get().satisfies(r -> assertFalse(r.success())));
	}

	@Nested
	public class MissingAttributeTest {
		@Test
		public void stringSuccessTest() {
			var suite = SuiteFactory.parseSuite(BASH_ASSERTION_MISSING_ATTRIBUTE);
			var executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.success()))
					.satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
							.satisfies(r -> assertFalse(r.success()))
							.satisfies(r -> assertEquals(
									asList(new Fail(FailReason.MISSING_ATTRIBUTES, "Missing attributes: [cmd]")),
									r.fails())));
		}
	}

	@Nested
	public class AssertContainsTest {

		@Test
		public void stringSuccessTest() {
			var suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_SUCCESS_CONTAINS);
			var executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.success()))
					.satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
							.satisfies(r -> assertTrue(r.success())));
		}

		@Test
		public void stringFailedTest() {
			var suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_FAILED_CONTAINS);
			var executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.success()))
					.satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
							.satisfies(r -> assertFalse(r.success())));
		}
	}

	@Nested
	public class AssertEqualsTest {

		@Test
		public void stringSuccessTest() {
			var suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_SUCCESS_EQUALS);
			var executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.success()))
					.satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
							.satisfies(r -> assertTrue(r.success())));
		}

		@Test
		public void stringFailedTest() {
			var suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_FAILED_EQUALS);
			var executor = new PlainTestExecutor();
			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.success()))
					.satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
							.satisfies(r -> assertFalse(r.success())));
		}
	}

	private Optional<Result> find(Result result, String name) {
		if (isNull(result)) {
			return Optional.empty();
		} else if (result.name().equals(name)) {
			return Optional.of(result);
		} else {
			return result.results().stream().map(r -> find((Result) r, name).orElse(null)).filter(Objects::nonNull)
					.findFirst();
		}
	}
}
