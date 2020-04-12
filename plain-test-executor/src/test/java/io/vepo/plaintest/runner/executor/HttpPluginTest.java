package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static io.vepo.plaintest.runner.executor.FailReason.ASSERTION;
import static io.vepo.plaintest.runner.executor.FailReason.RUNTIME_EXCEPTION;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.Delay.seconds;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.atLeast;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;

import io.vepo.plaintest.Suite;
import io.vepo.plaintest.SuiteFactory;

@ExtendWith(MockServerExtension.class)
@DisplayName("HTTP Executor")
public class HttpPluginTest extends AbstractTest {

	private MockServerClient client;

	@BeforeEach
	public void setup(MockServerClient client) {
		this.client = client;
	}

	@AfterEach
	public void tearDown() {
		client.reset();
	}

	private static final String HTTP_GET_TEST_SUITE = "Suite HttpGet {\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"}";

	@Test
	@DisplayName("It should execute GET")
	public void getTest() {
		InetSocketAddress remoteAddress = client.remoteAddress();
		validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 1, () -> {

			Suite suite = SuiteFactory.parseSuite(HTTP_GET_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
					.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())));
		});
	}

	private static final String HTTP_POST_TEST_SUITE = "Suite HttpPost {\n" + //
			"    HTTP PostRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultPost\"\n" + //
			"        method: \"POST\"\n" + //
			"        body: \"{ \\\"username\\\": \\\"vepo\\\" }\"\n" + //
			"    }\n" + //
			"}";

	@Test
	@DisplayName("It should execute POST")
	public void postTest() {
		InetSocketAddress remoteAddress = client.remoteAddress();
		validateHttp("/defaultPost", "POST", 201, "{ \"username\": \"vepo\" }", "{\"response\":\"CREATED\"}", 1, () -> {

			Suite suite = SuiteFactory.parseSuite(HTTP_POST_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
					.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpPost")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "PostRequest")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())));
		});

	}

	private static final String HTTP_INVALID_METHOD_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"INVALID_METHOD\"\n" + //
			"    }\n" + //
			"}";

	@Test
	@DisplayName("It should fail for invalid Method")
	public void invalidMethodTest() {
		InetSocketAddress remoteAddress = client.remoteAddress();
		validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, () -> {

			Suite suite = parseSuite(HTTP_INVALID_METHOD_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
					.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertFalse(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
						assertFalse(r.isSuccess());
						assertEquals(r.getFails(),
								asList(new Fail(RUNTIME_EXCEPTION, "Invalid Method: INVALID_METHOD")));
					}));
		});

	}

	private static final String HTTP_INVALID_URL_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"}";

	@Test
	@DisplayName("It should fail for invalid URL")
	public void invalidUrlTest() {
		InetSocketAddress remoteAddress = client.remoteAddress();
		validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, () -> {
			String port = Integer.toString(remoteAddress.getPort());
			Suite suite = parseSuite(
					HTTP_INVALID_URL_TEST_SUITE.replace("${host}", "THIS|IS|AN|INVALID|URL").replace("${port}", port));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertFalse(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
						assertFalse(r.isSuccess());

						assertEquals(
								asList(new Fail(RUNTIME_EXCEPTION,
										String.format("Invalid URL: THIS|IS|AN|INVALID|URL:%s/defaultGet", port))),
								r.getFails());
					}));
		});

	}

	@Test
	@DisplayName("It should fail for invalid URL")
	public void couldNotConnectTest() {
		InetSocketAddress remoteAddress = client.remoteAddress();
		validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, () -> {
			String port = Integer.toString(remoteAddress.getPort());
			Suite suite = parseSuite(
					HTTP_GET_TEST_SUITE.replace("${host}", "not-a-valid-endpoint.com.br").replace("${port}", port));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertFalse(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
						assertFalse(r.isSuccess());

						assertEquals(asList(new Fail(RUNTIME_EXCEPTION, String.format(
								"Could not connect with: http://not-a-valid-endpoint.com.br:%s/defaultGet. Unknown Host.",
								port))), r.getFails());
					}));
		});

	}

	private static final String HTTP_RESPONSE_CODE_ASSERTION_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"        assert statusCode Equals 200\n" + //
			"    }\n" + //
			"}";

	private static final String HTTP_BODY_ASSERTION_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"        assert content ${Verb} \"${Content}\"\n" + //
			"    }\n" + //
			"}";

	@Nested
	@DisplayName("Asserts")
	public class AssertTest {
		@Nested
		@DisplayName("Status Code")
		public class StatusCodeAssertTest {
			@Test
			@DisplayName("It should be possible to assert responseCode")
			public void responseCodeAssertionTest() {
				InetSocketAddress remoteAddress = client.remoteAddress();
				validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, () -> {
					String port = Integer.toString(remoteAddress.getPort());
					Suite suite = parseSuite(HTTP_RESPONSE_CODE_ASSERTION_TEST_SUITE
							.replace("${host}", remoteAddress.getHostName()).replace("${port}", port));
					PlainTestExecutor executor = new PlainTestExecutor();

					assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
							.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
									.satisfies(r -> assertTrue(r.isSuccess())))
							.satisfies(
									result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
										assertTrue(r.isSuccess());
										assertEquals(asList(), r.getFails());
									}));
				});
			}

			@Test
			@DisplayName("It should be possible to fail assert responseCode")
			public void responseCodeAssertionFailTest() {
				InetSocketAddress remoteAddress = client.remoteAddress();
				validateHttp("/defaultGet", "GET", 201, "{\"response\":\"OK\"}", 0, () -> {
					String port = Integer.toString(remoteAddress.getPort());
					Suite suite = parseSuite(HTTP_RESPONSE_CODE_ASSERTION_TEST_SUITE
							.replace("${host}", remoteAddress.getHostName()).replace("${port}", port));
					PlainTestExecutor executor = new PlainTestExecutor();

					assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
							.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
									.satisfies(r -> assertFalse(r.isSuccess())))
							.satisfies(
									result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
										assertFalse(r.isSuccess());
										assertEquals(asList(new Fail(ASSERTION, "statusCode is not equal to 200")),
												r.getFails());
									}));
				});
			}
		}

		@Nested
		@DisplayName("Body")
		public class BodyAssertTest {
			@Test
			@DisplayName("It should be possible to assert responseCode")
			public void bodyEqualsAssertionTest() {
				InetSocketAddress remoteAddress = client.remoteAddress();
				validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, () -> {
					String port = Integer.toString(remoteAddress.getPort());
					Suite suite = parseSuite(HTTP_BODY_ASSERTION_TEST_SUITE
							.replace("${host}", remoteAddress.getHostName()).replace("${port}", port)
							.replace("${Verb}", "Equals").replace("${Content}", "{\\\"response\\\":\\\"OK\\\"}"));
					PlainTestExecutor executor = new PlainTestExecutor();

					assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
							.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
									.satisfies(r -> assertTrue(r.isSuccess())))
							.satisfies(
									result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
										assertTrue(r.isSuccess());
										assertEquals(asList(), r.getFails());
									}));
				});
			}

			@Test
			@DisplayName("It should be possible to fail assert responseCode")
			public void responseCodeAssertionFailTest() {
				InetSocketAddress remoteAddress = client.remoteAddress();
				validateHttp("/defaultGet", "GET", 201, "{\"response\":\"OK\"}", 0, () -> {
					String port = Integer.toString(remoteAddress.getPort());
					Suite suite = parseSuite(HTTP_RESPONSE_CODE_ASSERTION_TEST_SUITE
							.replace("${host}", remoteAddress.getHostName()).replace("${port}", port));
					PlainTestExecutor executor = new PlainTestExecutor();

					assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
							.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
									.satisfies(r -> assertFalse(r.isSuccess())))
							.satisfies(
									result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
										assertFalse(r.isSuccess());
										assertEquals(asList(new Fail(ASSERTION, "statusCode is not equal to 200")),
												r.getFails());
									}));
				});
			}

		}
	}

	private static final String HTTP_TIMEOUT_URL_TEST_SUITE = "Suite HttpGet {\n" + //
			"\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"        timeout: 1000\n" + //
			"    }\n" + //
			"}";

	@Nested
	public class TimeoutTest {

		@Test
		@DisplayName("It should fail if execution time exceeds timeout")
		public void timeoutErrorTest() {
			InetSocketAddress remoteAddress = client.remoteAddress();
			validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 0, seconds(2), () -> {
				String port = Integer.toString(remoteAddress.getPort());
				Suite suite = parseSuite(HTTP_TIMEOUT_URL_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
						.replace("${port}", port));
				PlainTestExecutor executor = new PlainTestExecutor();

				assertThat(executor.execute(suite)).satisfies(result -> assertFalse(result.isSuccess()))
						.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
								.satisfies(r -> assertFalse(r.isSuccess())))
						.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get().satisfies(r -> {
							assertFalse(r.isSuccess());
							assertEquals(
									asList(new Fail(FailReason.TIMED_OUT, "Execution exceeds timeout! timeout=1000ms")),
									r.getFails());
						}));
			});
		}
	}

	private void validateHttp(String path, String method, int statusCode, String responseBody, int times, Delay delay,
			Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path);

		client.when(serverRequest)
				.respond(response().withStatusCode(statusCode).withBody(responseBody).withDelay(delay));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}

	private void validateHttp(String path, String method, int statusCode, String responseBody, int times,
			Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path);

		client.when(serverRequest).respond(response().withStatusCode(statusCode).withBody(responseBody));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}

	private void validateHttp(String path, String method, int statusCode, String requestBody, String responseBody,
			int times, Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path).withBody(requestBody);

		client.when(serverRequest).respond(response().withStatusCode(statusCode).withBody(responseBody));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}

}
