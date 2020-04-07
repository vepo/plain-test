package io.vepo.plaintest.runner.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.vepo.plaintest.SuiteFactory;

public class BashCommandTest {
	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	private static final String BASH_TEST;
	static {
		if (isWindows()) {
			BASH_TEST = """
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
					}""";
		} else {
			BASH_TEST = """
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
		}
	}

	@Test
	public void listCommandTest() {
		var suite = SuiteFactory.parseSuite(BASH_TEST);
		var executor = new PlainTestExecutor();
		assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.success()));
	}
}
