package io.vepo.plaintest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class SuiteTest {
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

		Suite.builder().step(Step.builder().index(10).name("Step10").build())
				.step(Step.builder().index(8).name("Step8").build()).step(Step.builder().index(6).name("Step6").build())
				.step(Step.builder().index(4).name("Step4").build()).step(Step.builder().index(2).name("Step2").build())
				.step(Step.builder().index(0).name("Step0").build())
				.suite(Suite.builder().index(9).name("Suite9").build())
				.suite(Suite.builder().index(7).name("Suite7").build())
				.suite(Suite.builder().index(5).name("Suite5").build())
				.suite(Suite.builder().index(3).name("Suite3").build())
				.suite(Suite.builder().index(1).name("Suite1").build()).build().forEachOrdered(suite -> {
					assertEquals(elements.pollLast(), suite);
					assertEquals(nextInteger.getAndIncrement(), suite.getIndex());
				}, step -> {
					assertEquals(elements.pollLast(), step);
					assertEquals(nextInteger.getAndIncrement(), step.getIndex());
				});

	}
}
