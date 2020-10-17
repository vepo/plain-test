package io.vepo.plaintest;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SuiteTest {

    @Test
    void builderTest() {
        Suite suite = Suite.builder().index(0).name("NAME").child(Suite.builder().index(1).name("S1").build())
                .child(Step.builder().name("Step1").build()).executionPath(Paths.get(".", "value1")).build();
        assertEquals(0, suite.getIndex());
        assertEquals("NAME", suite.getName());
        assertEquals(asList(Suite.builder().index(1).name("S1").build(), Step.builder().name("Step1").build()),
                suite.getChildren());
        assertEquals(Paths.get(".", "value1"), suite.getExecutionPath());
    }

    @Test
    @DisplayName("It should consume all elements ordered")
    void consumeOrderedTest() {
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

    @Test
    @DisplayName("It should consume all elements ordered with properties")
    void consumeOrderedWithPropertiesTest() {
        Deque<TestItem> elements = new LinkedList<>();
        elements.offerFirst(Step.builder().index(0).name("Step0").build());
        elements.offerFirst(Suite.builder().index(1).name("Suite1").build());
        elements.offerFirst(Properties.builder().index(2).value("key", "Property3").build());
        elements.offerFirst(Step.builder().index(3).name("Step3").build());
        elements.offerFirst(Suite.builder().index(4).name("Suite4").build());
        elements.offerFirst(Step.builder().index(5).name("Step5").build());
        elements.offerFirst(Suite.builder().index(6).name("Suite6").build());
        elements.offerFirst(Properties.builder().index(7).value("key", "Property7").build());
        elements.offerFirst(Step.builder().index(8).name("Step8").build());
        elements.offerFirst(Suite.builder().index(9).name("Suite9").build());
        elements.offerFirst(Step.builder().index(10).name("Step10").build());
        elements.offerFirst(Suite.builder().index(11).name("Suite11").build());
        elements.offerFirst(Step.builder().index(12).name("Step12").build());

        AtomicInteger nextInteger = new AtomicInteger(0);

        Suite.builder().child(Step.builder().index(12).name("Step12").build())
                .child(Step.builder().index(10).name("Step10").build())
                .child(Step.builder().index(8).name("Step8").build())
                .child(Step.builder().index(5).name("Step5").build())
                .child(Step.builder().index(3).name("Step3").build())
                .child(Step.builder().index(0).name("Step0").build())
                .child(Suite.builder().index(11).name("Suite11").build())
                .child(Suite.builder().index(9).name("Suite9").build())
                .child(Suite.builder().index(6).name("Suite6").build())
                .child(Suite.builder().index(4).name("Suite4").build())
                .child(Suite.builder().index(1).name("Suite1").build())
                .child(Properties.builder().index(7).value("key", "Property7").build())
                .child(Properties.builder().index(2).value("key", "Property3").build()).build()
                .forEachOrdered(suite -> {
                    assertEquals(elements.pollLast(), suite);
                    assertEquals(nextInteger.getAndIncrement(), suite.getIndex());
                }, step -> {
                    assertEquals(elements.pollLast(), step);
                    assertEquals(nextInteger.getAndIncrement(), step.getIndex());
                }, properties -> {
                    assertEquals(elements.pollLast(), properties);
                    assertEquals(nextInteger.getAndIncrement(), properties.getIndex());
                });

    }
}
