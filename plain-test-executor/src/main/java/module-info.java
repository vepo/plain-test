import io.vepo.plaintest.runner.executor.plugins.CommandExecutor;
import io.vepo.plaintest.runner.executor.plugins.StepExecutor;

module io.vepo.plaintest.runner {
	requires org.slf4j;
	requires io.vepo.plaintest.parser;
	requires ch.qos.logback.classic;

	provides StepExecutor with CommandExecutor;

	uses StepExecutor;
}