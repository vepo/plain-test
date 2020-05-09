package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static io.vepo.plaintest.runner.executor.FailReason.ASSERTION;
import static io.vepo.plaintest.runner.executor.FailReason.FAILED;
import static io.vepo.plaintest.runner.executor.FailReason.PLUGIN_NOT_FOUND;
import static io.vepo.plaintest.runner.executor.FailReason.RUNTIME_EXCEPTION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vepo.plaintest.Suite;

public class FailsTest extends AbstractHttpTest {

	@Test
	public void unknownPluginTest() {
		Suite suite = parseSuite("Suite UnknownTest {\n" + //
				"        Unknown DoNothing {\n" + //
				"            cmd    : \"xyz\"\n" + //
				"            timeout: 500\n" + //
				"            assert stdout Equals \"other string\"\n" + //
				"        }\n" + //
				"    }\n" + //
				"}");
		PlainTestExecutor executor = new PlainTestExecutor();
		Result result = executor.execute(suite);
		Result doNothingResult = result.getResults().stream().filter(r -> r.getName().equals("DoNothing")).findFirst()
				.orElse(null);
		assertNotNull(doNothingResult);
		assertEquals(asList(new Fail(PLUGIN_NOT_FOUND, "Could not find plugin: Unknown")), doNothingResult.getFails());
	}

	@Test
	public void unknownCommandPluginTest() {
		Suite suite = parseSuite("Suite UnknownTest {\n" + //
				"        Process DoNothing {\n" + //
				"            cmd    : \"unknownCommand\"\n" + //
				"            timeout: 500\n" + //
				"       }\n" + //
				"    }\n" + //
				"}");
		PlainTestExecutor executor = new PlainTestExecutor();
		Result result = executor.execute(suite);
		assertThat(find(result, "DoNothing")).isNotEmpty().map(Result::getFails).get().asList().hasSize(1)
				.allMatch(fail -> FAILED == ((Fail) fail).getReason());
	}

	@Test
	@DisplayName("It SHOULD assert on failed test")
	public void assertOnFailedTest() {
		Suite suite = parseSuite("Suite FailSuite {\n" + //
				"        HTTP GetRequest {\n" + //
				"            url: \"http://127.0.0.1:9999/defaultGet\"\n" + //
				"            method: \"GET\"\n" + //
				"            assert statusCode Equals null\n" + //
				"    }\n" + //
				"}");
		PlainTestExecutor executor = new PlainTestExecutor();
		Result result = executor.execute(suite);
		assertFalse(result.isSuccess());
		assertThat(find(result, "GetRequest")).isNotEmpty().map(Result::getFails).get().asList().containsExactly(
				new Fail(RUNTIME_EXCEPTION, "Could not connect with: http://127.0.0.1:9999/defaultGet"));
	}

	@Test
	@DisplayName("It SHOULD assert null values")
	public void assertOnNullFailTest() {
		InetSocketAddress address = remoteAddress();
		Suite suite = parseSuite(("Suite FailSuite {\n" + //
				"        HTTP GetRequest {\n" + //
				"            url: \"http://${URL}:${PORT}/some-path\"\n" + //
				"            method: \"GET\"\n" + //
				"            assert statusCode Equals null\n" + //
				"    }\n" + //
				"}").replace("${URL}", address.getHostName()).replace("${PORT}", Integer.toString(address.getPort())));
		validateHttp("/some-path", "GET", 200, "OK", 1, () -> {
			PlainTestExecutor executor = new PlainTestExecutor();
			Result result = executor.execute(suite);
			assertFalse(result.isSuccess());
			assertThat(find(result, "GetRequest")).isNotEmpty().map(Result::getFails).get().asList()
					.containsExactly(new Fail(ASSERTION, "statusCode is not equal to null"));
		});
	}

	@Test
	@DisplayName("It SHOULD assert not implemented")
	public void assertNotImplementedTest() {
		InetSocketAddress address = remoteAddress();
		Suite suite = parseSuite(("Suite FailSuite {\n" + //
				"        HTTP GetRequest {\n" + //
				"            url: \"http://${URL}:${PORT}/some-path\"\n" + //
				"            method: \"GET\"\n" + //
				"            assert statusCode Contains \"OK\"\n" + //
				"            assert statusCode Contains 200\n" + //
				"    }\n" + //
				"}").replace("${URL}", address.getHostName()).replace("${PORT}", Integer.toString(address.getPort())));
		validateHttp("/some-path", "GET", 200, "OK", 1, () -> {
			PlainTestExecutor executor = new PlainTestExecutor();
			Result result = executor.execute(suite);
			assertFalse(result.isSuccess());
			assertThat(find(result, "GetRequest")).isNotEmpty().map(Result::getFails).get().asList().containsExactly(
					new Fail(ASSERTION, "statusCode is not a String value."),
					new Fail(ASSERTION, "statusCode cannot check contains for numbers. value: 200"));
		});
	}
}
