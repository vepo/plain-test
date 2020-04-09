package io.vepo.plaintest.runner.executor.plugins;

import static io.vepo.plaintest.runner.executor.FailReason.RUNTIME_EXCEPTION;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.executor.Fail;
import io.vepo.plaintest.runner.executor.Result;
import io.vepo.plaintest.runner.executor.context.Context;

public class HttpExecutor implements StepExecutor {
	private static final Logger logger = LoggerFactory.getLogger(HttpExecutor.class);
	public static final String HTTP_EXECUTOR_PLUGIN_NAME = "HTTP";

	@Override
	public String name() {
		return HTTP_EXECUTOR_PLUGIN_NAME;
	}

	@Override
	public Result execute(Step step, Context context) {
		long start = currentTimeMillis();
		String stepUrl = step.attribute("url");
		String methodUrl = step.attribute("method");
		try {
			logger.info("Executing HTTP Request: url={} method={}", stepUrl, methodUrl);
			URL url = new URL(stepUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(methodUrl);
			int status = con.getResponseCode();
			String stdout = "";
			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				stdout = content.toString();
			}
			con.disconnect();
			logger.info("HTTP Request executed! output={}", stdout);
			return new Result(step.getName(), start, currentTimeMillis(), true, stdout, "", asList(), asList());
		} catch (MalformedURLException e) {
			logger.error("Error executing test.", e);
			return new Result(step.getName(), start, currentTimeMillis(), false, "", "Invalid URL: " + stepUrl,
					asList(), asList(new Fail(RUNTIME_EXCEPTION, "Invalid URL: " + stepUrl)));
		} catch (ProtocolException e) {
			logger.error("Error executing test.", e);
			return new Result(step.getName(), start, currentTimeMillis(), false, "", "Invalid Method: " + methodUrl,
					asList(), asList(new Fail(RUNTIME_EXCEPTION, "Invalid Method: " + methodUrl)));
		} catch (IOException e) {
			logger.error("Error executing test.", e);
			return new Result(step.getName(), start, currentTimeMillis(), false, "",
					"Could not connect with: " + stepUrl, asList(),
					asList(new Fail(RUNTIME_EXCEPTION, "Could not connect with: " + stepUrl)));
		}
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, Class<?>> requiredAttribute() {
		return new HashMap<String, Class<?>>() {
			{
				put("url", String.class);
				put("method", String.class);
			}
		};
	}

}