package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.runner.executor.FailReason.ASSERTION;
import static io.vepo.plaintest.runner.executor.FailReason.MISSING_ATTRIBUTES;
import static io.vepo.plaintest.runner.executor.FailReason.PLUGIN_NOT_FOUND;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Assertion;
import io.vepo.plaintest.PropertiesResolver;
import io.vepo.plaintest.Step;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.exceptions.PropertyNotDefinedException;
import io.vepo.plaintest.runner.executor.Result.ResultBuilder;
import io.vepo.plaintest.runner.executor.context.Context;
import io.vepo.plaintest.runner.executor.context.InnerSuiteContext;
import io.vepo.plaintest.runner.executor.context.RootSuiteContext;
import io.vepo.plaintest.runner.executor.plugins.StepExecutor;

public class PlainTestExecutor implements PropertiesResolver {
    private static final Logger logger = LoggerFactory.getLogger(PlainTestExecutor.class);
    private Map<String, StepExecutor> stepExecutors;
    private Properties properties;

    public PlainTestExecutor() {
        stepExecutors = new HashMap<>();
        ServiceLoader.load(StepExecutor.class)
                .forEach(stepExecutor -> stepExecutors.put(stepExecutor.name(), stepExecutor));
    }

    public Result execute(Suite suite) {
        suite.setPropertiesResolver(this);
        return executeSuite(suite,
                new RootSuiteContext(Optional.ofNullable(suite.getExecutionPath()).orElseGet(() -> Paths.get("."))));
    }

    private Result executeSuite(Suite suite, Context context) {
        AtomicBoolean successReference = new AtomicBoolean(true);
        ResultBuilder resultBuilder = Result.builder().name(suite.getName()).start(currentTimeMillis());
        suite.forEachOrdered(innerSuite -> {
            Context innerContext = new InnerSuiteContext(context,
                    context.getWorkingDirectory().resolve(innerSuite.getExecutionPath()).toAbsolutePath());
            Result suiteResult = executeSuite(innerSuite, innerContext);
            logger.debug("Suite Executed! results={}", suiteResult);
            successReference.set(successReference.get() && suiteResult.isSuccess());
            resultBuilder.result(suiteResult);
            context.addResult(suiteResult);
        }, innerStep -> {
            Result stepResult = executeStep(innerStep, context);
            logger.debug("Step Executed! results={}", stepResult);
            successReference.set(successReference.get() && stepResult.isSuccess());
            resultBuilder.result(stepResult);
            context.addResult(stepResult);
        });
        return resultBuilder.end(currentTimeMillis()).success(successReference.get()).build();
    }

    private Result executeStep(Step step, Context context) {
        if (stepExecutors.containsKey(step.getPlugin())) {
            StepExecutor executor = stepExecutors.get(step.getPlugin());
            Set<Attribute<?>> missingAttributes = executor.requiredAttribute().filter(Attribute::isRequired)
                    .filter(entry -> !step.getAttributes().containsKey(entry.getKey())).collect(toSet());
            if (!missingAttributes.isEmpty()) {
                return Result.builder().name(step.getName()).success(false)
                        .fail(new Fail(MISSING_ATTRIBUTES, "Missing attributes: ["
                                + missingAttributes.stream().map(Attribute::getKey).collect(joining(", ")) + "]"))
                        .build();
            } else {
                return checkAssertions(step, executor.execute(step, context));
            }
        } else {
            return Result.builder().name(step.getName()).success(false)
                    .fail(new Fail(PLUGIN_NOT_FOUND, "Could not find plugin: " + step.getPlugin())).build();
        }
    }

    private Result checkAssertions(Step step, Result result) {
        ResultBuilder builder = Result.builder(result);
        Consumer<Fail> failCallback = fail -> builder.success(false).fail(fail);
        step.getAssertions().forEach(assertion -> {
            switch (assertion.getVerb()) {
                case "Contains":
                    checkAssertionContains(result, assertion, failCallback);
                    break;

                case "Equals":
                    checkAssertionEquals(result, assertion, failCallback);
                    break;
                default:
                    // nothing
            }
        });
        return builder.build();
    }

    private void checkAssertionEquals(Result result, Assertion<?> assertion, Consumer<Fail> failCallback) {
        if (isNull(assertion.getValue())) {
            Object value = result.get(assertion.getProperty(), Object.class);
            if (nonNull(value)) {
                failCallback.accept(new Fail(ASSERTION, assertion.getProperty() + " is not equal to null"));
            }
        } else if (assertion.getValue() instanceof String) {
            String value = result.get(assertion.getProperty(), String.class);
            if (value.compareTo((String) assertion.getValue()) != 0) {
                failCallback.accept(
                        new Fail(ASSERTION, assertion.getProperty() + " is not equal to " + assertion.getValue()));
            }
        } else if (assertion.getValue() instanceof Long) {
            Long value = result.get(assertion.getProperty(), Long.class);
            if ((nonNull(value) && value.longValue() != ((Long) assertion.getValue()).longValue())
                    || isNull(value) && nonNull(assertion.getValue())) {
                failCallback.accept(
                        new Fail(ASSERTION, assertion.getProperty() + " is not equal to " + assertion.getValue()));
            }
        }
    }

    private void checkAssertionContains(Result result, Assertion<?> assertion, Consumer<Fail> failCallback) {
        if (assertion.getValue() instanceof String) {
            String value = result.get(assertion.getProperty(), String.class);
            if (nonNull(value)) {
                if (!value.contains((String) assertion.getValue())) {
                    failCallback.accept(new Fail(ASSERTION,
                            assertion.getProperty() + " does not contains " + assertion.getValue()));
                }
            } else {
                failCallback.accept(new Fail(ASSERTION, assertion.getProperty() + " is not a String value."));
            }
        } else {
            failCallback.accept(new Fail(ASSERTION,
                    assertion.getProperty() + " cannot check contains for numbers. value: " + assertion.getValue()));
        }
    }

    public void load(Properties properties) {
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T findRequiredPropertyValue(String key) {
        if (nonNull(properties) && properties.containsKey(key)) {
            return (T) properties.get(key);
        }
        throw new PropertyNotDefinedException("Could not find property: " + key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> findOptionalPropertyValue(String key) {
        if (nonNull(properties) && properties.containsKey(key)) {
            return Optional.of((T) properties.get(key));
        }
        return Optional.empty();
    }
}
