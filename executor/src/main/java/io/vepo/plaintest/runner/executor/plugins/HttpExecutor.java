package io.vepo.plaintest.runner.executor.plugins;

import static io.vepo.plaintest.runner.executor.Attribute.createAttribute;
import static io.vepo.plaintest.runner.executor.FailReason.RUNTIME_EXCEPTION;
import static io.vepo.plaintest.runner.executor.FailReason.TIMED_OUT;
import static io.vepo.plaintest.runner.utils.Timeout.executeWithTimeout;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.executor.Attribute;
import io.vepo.plaintest.runner.executor.Fail;
import io.vepo.plaintest.runner.executor.Result;
import io.vepo.plaintest.runner.executor.Result.ResultBuilder;
import io.vepo.plaintest.runner.executor.context.Context;

public class HttpExecutor implements StepExecutor {
	private static final Logger logger = LoggerFactory.getLogger(HttpExecutor.class);
	public static final String HTTP_EXECUTOR_PLUGIN_NAME = "HTTP";

	public static class HttpContents {
		private final int statusCode;
		private final String body;
		private final Exception exception;

		private HttpContents(int statusCode, String body, Exception exception) {
			this.statusCode = statusCode;
			this.body = body;
			this.exception = exception;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getBody() {
			return body;
		}

		public Exception getException() {
			return exception;
		}
	}

	@Override
	public String name() {
		return HTTP_EXECUTOR_PLUGIN_NAME;
	}

	@Override
	public Result execute(Step step, Context context) {
		ResultBuilder resultBuilder = Result.builder().name(step.getName()).start(currentTimeMillis());
		String stepUrl = step.requiredAttribute("url");
		String methodUrl = step.requiredAttribute("method");
		Optional<Long> maybeTimeout = step.optionalAttribute("timeout", Long.class);
		Optional<HttpContents> maybeContents = executeWithTimeout(
				() -> executeRequest(stepUrl, methodUrl, step.optionalAttribute("body", String.class)), maybeTimeout);
		return maybeContents.map(contents -> {
			resultBuilder.end(currentTimeMillis());
			if (isNull(contents.getException())) {
				resultBuilder.success(true).property("statusCode", contents.getStatusCode()).property("content",
						contents.getBody());
			} else {
				processError(resultBuilder, stepUrl, methodUrl, contents);
			}

			return resultBuilder.build();
		}).orElseGet(
				() -> resultBuilder.end(currentTimeMillis()).success(false)
						.fail(new Fail(TIMED_OUT,
								String.format("Execution exceeds timeout! timeout=%dms", maybeTimeout.orElse(-1L))))
						.build());
	}

	private HttpContents executeRequest(String stepUrl, String methodUrl, Optional<String> maybeRequestBody) {
		try {
			logger.info("Executing HTTP Request: url={} method={}", stepUrl, methodUrl);
			URL url = new URL(stepUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(methodUrl);

			Optional<IOException> sendException = maybeRequestBody.map(requestBody -> {
				con.setDoOutput(true);
				try (OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream())) {
					out.write(requestBody);
					return null;
				} catch (IOException e) {
					return e;
				}
			});

			if (sendException.isPresent()) {
				return new HttpContents(-1, "", sendException.get());
			} else {
				int status = con.getResponseCode();
				String body = "";
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
					String inputLine;
					StringBuilder content = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						content.append(inputLine);
					}
					body = content.toString();
				}
				con.disconnect();
				logger.info("HTTP Request executed! output={}", body);
				return new HttpContents(status, body, null);
			}
		} catch (IOException e) {
			return new HttpContents(-1, "", e);
		}
	}

	private void processError(ResultBuilder resultBuilder, String stepUrl, String methodUrl, HttpContents contents) {
		resultBuilder.success(false);
		if (contents.getException() instanceof MalformedURLException) {
			resultBuilder.fail(new Fail(RUNTIME_EXCEPTION, "Invalid URL: " + stepUrl));
		} else if (contents.getException() instanceof ProtocolException) {
			resultBuilder.fail(new Fail(RUNTIME_EXCEPTION, "Invalid Method: " + methodUrl));
		} else if (contents.getException() instanceof UnknownHostException) {
			resultBuilder.fail(new Fail(RUNTIME_EXCEPTION, "Could not connect with: " + stepUrl + ". Unknown Host."));
		} else if (contents.getException() instanceof IOException) {
			resultBuilder.fail(new Fail(RUNTIME_EXCEPTION, "Could not connect with: " + stepUrl));
		} else {
			throw new IllegalStateException("Exception not implemented!", contents.getException());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stream<Attribute<?>> requiredAttribute() {
		return Stream.of(createAttribute("url", String.class, true), createAttribute("method", String.class, true),
				createAttribute("timeout", Long.class, false), createAttribute("body", String.class, false));
	}

}