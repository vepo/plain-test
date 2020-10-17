package io.vepo.plaintest.runner.executor;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.vepo.plaintest.Suite;
import io.vepo.plaintest.SuiteFactory;
import io.vepo.plaintest.runner.utils.Os;
import io.vepo.plaintest.runner.utils.Os.OS;

@DisplayName("Command Executor")
class CommandExecutorTest extends AbstractTest {

    private static final String BASH_SUCCESS_TEST = Os.getOS() == OS.WINDOWS ? "Suite BashTest {\n" + //
            "    path: src\n" + //
            "\n" + //
            "    Process EnterDir {\n" + //
            "        cmd    : \"dir\"\n" + //
            "        timeout: 500\n" + //
            "        assert stdout Contains \"main\"\n" + //
            "    }\n" + //
            "\n" + //
            "    Suite MainTest {\n" + //
            "        path: main\n" + //
            "        Process EnterSubFolder {\n" + //
            "            cmd    : \"dir\"\n" + //
            "            timeout: 500\n" + //
            "            assert stdout Contains \"java\"\n" + //
            "        }\n" + //
            "    }\n" + //
            "}"
            : "Suite BashTest {\n" + //
                    "    path: src\n" + //
                    "\n" + //
                    "    Process EnterDir {\n" + //
                    "        cmd    : \"ls\"\n" + //
                    "        timeout: 500\n" + //
                    "        assert stdout Contains \"main\"\n" + //
                    "    }\n" + //
                    "\n" + //
                    "    Suite MainTest {\n" + //
                    "        path: main\n" + //
                    "        Process EnterSubFolder {\n" + //
                    "            cmd    : \"ls\"\n" + //
                    "            timeout: 500\n" + //
                    "            assert stdout Contains \"java\"\n" + //
                    "        }\n" + //
                    "    }\n" + //
                    "}";

    private static final String BASH_FAIL_TEST = "Suite BashTest {\n" + //
            "\n" + //
            "    Process Error {\n" + //
            "        cmd    : \"commandThatDoesNotExists\"\n" + //
            "        timeout: 500\n" + //
            "    }\n" + //
            "}";

    private static final String BASH_FAIL_ASSERTION = Os.getOS() == OS.WINDOWS ? "Suite BashTest {\n" + //
            "\n" + //
            "Process Error {\n" + //
            "    cmd    : \"dir\"\n" + //
            "    timeout: 500\n" + //
            "    assert stdout Contains \"String That DOES NOT EXIST!\"\n" + //
            "}\n" + //
            "}"
            : "Suite BashTest {\n" + //
                    "\n" + //
                    "    Process Error {\n" + //
                    "        cmd    : \"ls\"\n" + //
                    "        timeout: 500\n" + //
                    "        assert stdout Contains \"String That DOES NOT EXIST!\"\n" + //
                    "    }\n" + //
                    "}";

    private static final String BASH_ASSERTION_MISSING_ATTRIBUTE = "Suite EchoTest {\n" + //
            "\n" + //
            "    Process EchoSomeString {\n" + //
            "        timeout: 500\n" + //
            "        assert stdout Equals \"other string\"\n" + //
            "    }\n" + //
            "}";

    @Test
    void listCommandTest() {
        Suite suite = SuiteFactory.parseSuite(BASH_SUCCESS_TEST);
        PlainTestExecutor executor = new PlainTestExecutor();
        assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
                .satisfies(result -> assertThat(find(result, "EnterDir")).isPresent().get()
                        .satisfies(r -> assertTrue(r.isSuccess())))
                .satisfies(result -> assertThat(find(result, "MainTest")).isPresent().get()
                        .satisfies(r -> assertTrue(r.isSuccess())));
    }

    @Test
    void failCommandTest() {
        Suite suite = SuiteFactory.parseSuite(BASH_FAIL_TEST);
        PlainTestExecutor executor = new PlainTestExecutor();
        assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
                .satisfies(result -> assertThat(find(result, "Error")).isPresent().get()
                        .satisfies(r -> assertFalse(r.isSuccess())));
    }

    @Test
    void failAssertionCommandTest() {
        Suite suite = SuiteFactory.parseSuite(BASH_FAIL_ASSERTION);
        PlainTestExecutor executor = new PlainTestExecutor();
        assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
                .satisfies(result -> assertThat(find(result, "Error")).isPresent().get()
                        .satisfies(r -> assertFalse(r.isSuccess())));
    }

    @Nested
    class MissingAttributeTest {
        @Test
        void stringSuccessTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_MISSING_ATTRIBUTE);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
                            .satisfies(r -> assertFalse(r.isSuccess()))
                            .satisfies(r -> assertEquals(
                                    asList(new Fail(FailReason.MISSING_ATTRIBUTES, "Missing attributes: [cmd]")),
                                    r.getFails())));
        }
    }

    private static final String BASH_ASSERTION_STRING_SUCCESS_CONTAINS = "Suite EchoTest {\n" + //
            "\n" + //
            "    Process EchoSomeString {\n" + //
            "        cmd    : \"echo some string\"\n" + //
            "        timeout: 500\n" + //
            "        assert stdout Equals \"some string\"\n" + //
            "    }\n" + //
            "}";

    private static final String BASH_ASSERTION_STRING_FAILED_CONTAINS = "Suite EchoTest {\n" + //
            "\n" + //
            "    Process EchoSomeString {\n" + //
            "        cmd    : \"echo some string\"\n" + //
            "        timeout: 500\n" + //
            "        assert stdout Equals \"other string\"\n" + //
            "    }\n" + //
            "}";

    @Nested
    class AssertContainsTest {

        @Test
        void stringSuccessTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_SUCCESS_CONTAINS);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
                            .satisfies(r -> assertTrue(r.isSuccess())));
        }

        @Test
        void stringFailedTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_FAILED_CONTAINS);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
                            .satisfies(r -> assertFalse(r.isSuccess())));
        }
    }

    private static final String BASH_ASSERTION_STRING_SUCCESS_EQUALS = "Suite EchoTest {\n" + //
            "\n" + //
            "    Process EchoSomeString {\n" + //
            "        cmd    : \"echo some string\"\n" + //
            "        timeout: 500\n" + //
            "        assert stdout Equals \"some string\"\n" + //
            "    }\n" + //
            "}";

    private final String BASH_ASSERTION_STRING_FAILED_EQUALS = "Suite EchoTest {\n" + //
            "\n" + //
            "    Process EchoSomeString {\n" + //
            "        cmd    : \"echo some string\"\n" + //
            "        timeout: 500\n" + //
            "        assert stdout Equals \"string\"\n" + //
            "    }\n" + //
            "}";

    @Nested
    class AssertEqualsTest {

        @Test
        void stringSuccessTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_SUCCESS_EQUALS);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
                            .satisfies(r -> assertTrue(r.isSuccess())));
        }

        @Test
        void stringFailedTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_ASSERTION_STRING_FAILED_EQUALS);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "EchoSomeString")).isPresent().get()
                            .satisfies(r -> assertFalse(r.isSuccess())));
        }
    }

    private static final String BASH_SLEEP_10s_FAIL = Os.getOS() == OS.WINDOWS ? "Suite SleepTest {\n" + //
            "\n" + //
            "    Process Sleep10s {\n" + //
            "        cmd    : \"ping -n 10 127.0.0.1\"\n" + // Why?!?!
                                                            // https://www.ibm.com/support/pages/timeout-command-run-batch-job-exits-immediately-and-returns-error-input-redirection-not-supported-exiting-process-immediately
            "        timeout: 1000\n" + //
            "    }\n" + //
            "}"
            : "Suite SleepTest {\n" + //
                    "\n" + //
                    "    Process Sleep10s {\n" + //
                    "        cmd    : \"sleep 10\"\n" + //
                    "        timeout: 1000\n" + //
                    "    }\n" + //
                    "}";

    private static final String BASH_SLEEP_2s_SUCCESS = Os.getOS() == OS.WINDOWS ? "Suite SleepTest {\n" + //
            "\n" + //
            "    Process Sleep2s {\n" + //
            "        cmd    : \"ping -n 3 127.0.0.1\"\n" + // Why?!?!
                                                           // https://www.ibm.com/support/pages/timeout-command-run-batch-job-exits-immediately-and-returns-error-input-redirection-not-supported-exiting-process-immediately
            "        timeout: 2000\n" + //
            "    }\n" + //
            "}"
            : "Suite SleepTest {\n" + //
                    "\n" + //
                    "    Process Sleep2s {\n" + //
                    "        cmd    : \"sleep 2\"\n" + //
                    "        timeout: 2000\n" + //
                    "    }\n" + //
                    "}";
    private static final String BASH_TIMEOUT_OPTIONAL = "Suite OptionalTest {\n" + //
            "\n" + //
            "    Process OptionalStep {\n" + //

            (Os.getOS() == OS.WINDOWS ?

                    "        cmd    : \"dir\"\n" : //
                    "        cmd    : \"ls\"\n"

            ) + //
            "    }\n" + //
            "}";

    @Nested
    @DisplayName("Timeout Tests")
    class AssertTimeoutTest {

        @Test
        @DisplayName("It should fail because of timeout")
        void timeoutTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_SLEEP_10s_FAIL);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "Sleep10s")).isPresent().get().satisfies(r -> {
                        assertFalse(r.isSuccess());
                        assertEquals(
                                asList(new Fail(FailReason.TIMED_OUT, "Execution exceeds timeout! timeout=1000ms")),
                                r.getFails());
                    }));
        }

        @Test
        @DisplayName("It should not fail for exact time")
        void exactTimeTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_SLEEP_2s_SUCCESS);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "Sleep2s")).isPresent().get()
                            .satisfies(r -> assertTrue(r.isSuccess())));
        }

        @Test
        @DisplayName("It should be an optional property")
        void optionalTest() {
            Suite suite = SuiteFactory.parseSuite(BASH_TIMEOUT_OPTIONAL);
            PlainTestExecutor executor = new PlainTestExecutor();
            assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
                    .satisfies(result -> assertThat(find(result, "OptionalStep")).isPresent().get()
                            .satisfies(r -> assertTrue(r.isSuccess())));
        }
    }
}
