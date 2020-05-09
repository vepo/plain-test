package io.vepo.plaintest.examples.validation;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.vepo.plaintest.SuiteFactory;
import io.vepo.plaintest.runner.executor.PlainTestExecutor;
import io.vepo.plaintest.runner.executor.Result;

public class ExamplesValidationTest {

	@ParameterizedTest
	@MethodSource
	public void validateExamplesTest(File suite) throws IOException {
		PlainTestExecutor executor = new PlainTestExecutor();
		Result result = executor.execute(SuiteFactory.parseSuite(new String(readAllBytes(suite.toPath()))));
		assertTrue(result.isSuccess());
	}

	public static Stream<File> validateExamplesTest() {
		Path exampleFolder = Paths.get("..", "examples");
		if (!exampleFolder.toFile().exists()) {
			exampleFolder = Paths.get(".", "examples");
		}

		String absolutePath = exampleFolder.toAbsolutePath().toString();
		return Arrays.stream(exampleFolder.toFile().list()).map(filename -> Paths.get(absolutePath, filename))
				.map(Path::toFile);
	}
}
