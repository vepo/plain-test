package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class SuiteTest {

	@Test
	public void builderTest() {
		Suite suite = Suite.builder().index(0).name("NAME").child(Suite.builder().index(1).name("S1").build())
				.child(Step.builder().name("Step1").build()).attribute(EXECUTION_PATH, "value1").build();
		assertEquals(0, suite.getIndex());
		assertEquals("NAME", suite.getName());
		assertEquals(asList(Suite.builder().index(1).name("S1").build(), Step.builder().name("Step1").build()),
				suite.getChildren());
		assertEquals(Collections.singletonMap(EXECUTION_PATH, "value1"), suite.getAttributes());
	}

	@Test
	public void attributeTest() {
		assertThat(Suite.builder().attribute(EXECUTION_PATH, "x").build().attribute(EXECUTION_PATH)).isPresent()
				.hasValue("x");
		assertThat(Suite.builder().build().attribute(EXECUTION_PATH)).isNotPresent();
		assertThat(Suite.builder().attribute(EXECUTION_PATH, "x").build().attribute(EXECUTION_PATH, String.class))
				.isPresent().hasValue("x");
		assertThat(Suite.builder().build().attribute(EXECUTION_PATH, String.class)).isNotPresent();

		assertThatThrownBy(
				() -> Suite.builder().attribute(EXECUTION_PATH, "x").build().attribute(EXECUTION_PATH, Long.class))
						.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void consumeOrderedTest() {
		Deque<Object> elements = new LinkedList<>();
		elements.offerFirst(Step.builder().index(0).name("Step0").build());
		elements.offerFirst(Suite.builder().index(1).name("Suite1").build());
		elements.offerFirst(Step.builder().index(2).name("Step2").build());
		elements.offerFirst(Suite.builder().index(3).name("Suite3").build());
		elements.offerFirst(Step.builder().index(4).name("Step4").build());
		elements.offerFirst(Suite.builder().index(5).name("Suite5").build());
		elements.offerFirst(Step.builder().index(6).name("Step6").build());
		elements.offerFirst(Suite.builder().index(7).name("Suite7").build());
		elements.offerFirst(Step.builder().index(8).name("Step8").build());
		elements.offerFirst(Suite.builder().index(9).name("Suite9").build());
		elements.offerFirst(Step.builder().index(10).name("Step10").build());

		AtomicInteger nextInteger = new AtomicInteger(0);

		Suite.builder().child(Step.builder().index(10).name("Step10").build())
				.child(Step.builder().index(8).name("Step8").build())
				.child(Step.builder().index(6).name("Step6").build())
				.child(Step.builder().index(4).name("Step4").build())
				.child(Step.builder().index(2).name("Step2").build())
				.child(Step.builder().index(0).name("Step0").build())
				.child(Suite.builder().index(9).name("Suite9").build())
				.child(Suite.builder().index(7).name("Suite7").build())
				.child(Suite.builder().index(5).name("Suite5").build())
				.child(Suite.builder().index(3).name("Suite3").build())
				.child(Suite.builder().index(1).name("Suite1").build()).build().forEachOrdered(suite -> {
					assertEquals(elements.pollLast(), suite);
					assertEquals(nextInteger.getAndIncrement(), suite.getIndex());
				}, step -> {
					assertEquals(elements.pollLast(), step);
					assertEquals(nextInteger.getAndIncrement(), step.getIndex());
				});

	}
}
