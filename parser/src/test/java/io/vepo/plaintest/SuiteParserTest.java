package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.vepo.plaintest.PropertiesSource.SourceType;
import io.vepo.plaintest.Suite.SuiteBuilder;
import io.vepo.plaintest.exceptions.PropertyNotDefinedException;

public class SuiteParserTest {

    @Nested
    public class ExecutionDirectoryTest {
        @Test
        @DisplayName("It SHOULD parse a Suite with execution directory absolute Unix Path")
        public void absoluteUnixPathTest() {
            assertEquals(Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "/opt/xyz/qasd").build(),
                    parseSuite("Suite T1 {\n" + //
                            "    exec-dir: /opt/xyz/qasd\n" + //
                            "}"));
        }

        @Test
        @DisplayName("It SHOULD parse a Suite with execution directory absolute Windows Path")
        public void absoluteWindowsPathTest() {
            assertEquals(Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "C:\\user\\xyx").build(),
                    parseSuite("Suite T1 {\n" + //
                            "    exec-dir: C:\\user\\xyx\n" + //
                            "}"));
            ;
        }

        @Test
        @DisplayName("It SHOULD parse a Suite with execution directory relative Unix Path")
        public void relativeUnixPathTest() {
            assertEquals(Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "./src/main/java").build(),
                    parseSuite("Suite T1 {\n" + //
                            "    exec-dir: ./src/main/java\n" + //
                            "}"));
        }

        @Test
        @DisplayName("It SHOULD parse a Suite with execution directory relative Windows Path")
        public void relativeWindowsPathTest() {
            assertEquals(Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, ".\\src\\main\\java").build(),
                    parseSuite("Suite T1 {\n" + //
                            "    exec-dir: .\\src\\main\\java\n" + //
                            "}"));
        }

        @Test
        @DisplayName("It SHOULD parse a Suite with execution directory  Unix Path")
        public void unixPathTest() {
            assertEquals(Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "src/main/java").build(),
                    parseSuite("Suite T1 {\n" + //
                            "    exec-dir: src/main/java\n" + //
                            "}"));
        }

        @Test
        @DisplayName("It SHOULD parse a Suite with execution directory Windows Path")
        public void windowsPathTest() {
            assertEquals(Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "src\\main\\java").build(),
                    parseSuite("Suite T1 {\n" + //
                            "    exec-dir: src\\main\\java\n" + //
                            "}"));
        }

        @Test
        @DisplayName("It SHOULD parse a Suite with execution directory simple filename")
        public void simpleTest() {
            assertEquals(Suite.builder().index(0).name("T1").attribute(EXECUTION_PATH, "src").build(),
                    parseSuite("Suite T1 {\n" + //
                            "    exec-dir: src\n" + //
                            "}"));
        }

    }

    @Test
    @DisplayName("It SHOULD parse an empty Suite")
    public void emptySuiteParseTest() {
        assertEquals(Suite.builder().index(0).name("T1").build(), parseSuite("Suite T1 { }"));
    }

    @Test
    @DisplayName("It SHOULD parse no Suite")
    public void noSuiteParseTest() {
        assertNull(parseSuite(""));
    }

    @Test
    @DisplayName("It SHOULD parse index of inner Steps")
    public void indexTest() {
        SuiteBuilder t1Builder = Suite.builder().index(0).name("T1");
        Suite suite = parseSuite("Suite T1 {\n" + //
                "    HTTP Step1 {\n" + //
                "        method  : \"GET\"\n" + //
                "        assert responseCode Equals 200\n" + //
                "        assert body Equals \"\"\"\n" + //
                "                           Hello World!\"\"\"" + //
                "    }\n" + //
                "    Process Step2 {\n" + //
                "        cmd: \"ls\"\n" + //
                "    }\n" + //
                "    Process Step3 {\n" + //
                "        cmd: \"cd new-folder\"\n" + //
                "    }\n" + //
                "    Suite T2 {\n" + //
                "        Process Step2.1 {\n" + //
                "            cmd: \"ls\"\n" + //
                "        }\n" + //
                "    }\n" + //
                "    Process Step4 {\n" + //
                "        cmd: \"ls *.txt\"\n" + //
                "    }\n" + //
                "}");
        assertEquals(t1Builder
                .child(Step.builder().parent(t1Builder).index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
                        .assertion(new Assertion<>("responseCode", "Equals", 200L))
                        .assertion(new Assertion<>("body", "Equals", "Hello World!")).build())
                .child(Step.builder().parent(t1Builder).index(1).plugin("Process").name("Step2").attribute("cmd", "ls")
                        .build())
                .child(Step.builder().parent(t1Builder).index(2).plugin("Process").name("Step3")
                        .attribute("cmd", "cd new-folder").build())
                .child(Suite.builder().parent(t1Builder).index(3).name("T2")
                        .child(Step.builder().index(0).plugin("Process").name("Step2.1").attribute("cmd", "ls").build())
                        .build())
                .child(Step.builder().parent(t1Builder).index(4).plugin("Process").name("Step4")
                        .attribute("cmd", "ls *.txt").build())
                .build(), suite);
        assertEquals(0, suite.getIndex());
        assertEquals("T1", suite.getName());

        Step step1 = (Step) suite.getChild(0);
        assertEquals(0, step1.getIndex());
        assertEquals("Step1", step1.getName());
        assertEquals("HTTP", step1.getPlugin());
        assertEquals(singletonMap("method", "GET"), step1.getAttributes());
        assertEquals(asList(new Assertion<>("responseCode", "Equals", 200L),
                new Assertion<>("body", "Equals", "Hello World!")), step1.getAssertions());
        assertEquals(suite, step1.getParent());

        Assertion<?> responseCodeAssertion = step1.getAssertions().get(0);
        assertEquals("responseCode", responseCodeAssertion.getProperty());
        assertEquals("Equals", responseCodeAssertion.getVerb());
        assertEquals(200L, responseCodeAssertion.getValue());
    }

    @Test
    @DisplayName("It SHOULD parse Suite with inner Suites")
    public void innerSuiteTest() {
        SuiteBuilder t1Builder = Suite.builder();
        SuiteBuilder t2Builder = Suite.builder();
        SuiteBuilder t3Builder = Suite.builder();
        Suite suite = parseSuite("Suite T1 {\n" + //
                "    Properties {\n" + //
                "        method_get: \"GET\"\n" + //
                "    }\n" + //
                "    HTTP Step1 {\n" + //
                "        method: ${method_get}\n" + //
                "    }\n" + //
                "    Suite T2 {\n" + //
                "        HTTP Step2 {\n" + //
                "            method: \"POST\"\n" + //
                "        }\n" + //
                "        Suite T3 {\n" + //
                "            Properties {\n" + //
                "                method_put: \"PUT\"\n" + //
                "            }\n" + //
                "            HTTP Step3 {\n" + //
                "                method: ${method_put}\n" + //
                "            }\n" + //
                "        }\n" + //
                "    }\n" + //
                "}");
        assertEquals(t1Builder.index(0).name("T1")
                .child(Properties.builder().index(0).parent(t1Builder).value("method_get", "GET").build())
                .child(Step.builder().parent(t1Builder).index(1).plugin("HTTP").name("Step1")
                        .attribute("method", new PropertyReference("method_get")).build())
                .child(t2Builder.parent(t1Builder).index(2).name("T2")
                        .child(Step.builder().parent(t2Builder).index(0).plugin("HTTP").name("Step2")
                                .attribute("method", "POST").build())
                        .child(t3Builder.parent(t2Builder).index(1).name("T3")
                                .child(Properties.builder().parent(t3Builder).index(0).value("method_put", "PUT")
                                        .build())
                                .child(Step.builder().parent(t3Builder).index(1).plugin("HTTP").name("Step3")
                                        .attribute("method", new PropertyReference("method_put")).build())
                                .build())
                        .build())
                .build(), suite);

        assertEquals("T1", suite.getName());
        assertEquals(0, suite.getIndex());
        assertNull(suite.getParent());

        Properties t1Properties = (Properties) suite.getChild(0);
        assertEquals(0, t1Properties.getIndex());
        assertEquals(Collections.singletonMap("method_get", "GET"), t1Properties.getValues());
        assertEquals(suite, t1Properties.getParent());

        Step step1 = (Step) suite.getChild(1);
        assertEquals(1, step1.getIndex());
        assertEquals("HTTP", step1.getPlugin());
        assertEquals("Step1", step1.getName());
        assertEquals(singletonMap("method", new PropertyReference("method_get")), step1.getAttributes());
        assertEquals(suite, step1.getParent());

        Suite t2 = (Suite) suite.getChild(2);
        assertEquals(2, t2.getIndex());
        assertEquals("T2", t2.getName());
        assertEquals(suite, t2.getParent());

        Step step2 = (Step) t2.getChild(0);
        assertEquals(0, step2.getIndex());
        assertEquals("HTTP", step2.getPlugin());
        assertEquals("Step2", step2.getName());
        assertEquals(singletonMap("method", "POST"), step2.getAttributes());
        assertEquals(t2, step2.getParent());

        Suite t3 = (Suite) t2.getChild(1);
        assertEquals(1, t3.getIndex());
        assertEquals("T3", t3.getName());
        assertEquals(t2, t3.getParent());

        Properties t3Properties = (Properties) t3.getChild(0);
        assertEquals(0, t3Properties.getIndex());
        assertEquals(singletonMap("method_put", "PUT"), t3Properties.getValues());

        Step step3 = (Step) t3.getChild(1);
        assertEquals(1, step3.getIndex());
        assertEquals("HTTP", step3.getPlugin());
        assertEquals("Step3", step3.getName());
        assertEquals(singletonMap("method", new PropertyReference("method_put")), step3.getAttributes());
        assertEquals(t3, step3.getParent());

    }

    @Test
    @DisplayName("It SHOULD parse parameter value Multi Line String")
    public void multilineStringTest() {
        assertEquals(Suite.builder().index(0).name("T1").child(Step.builder().index(0).plugin("HTTP").name("Step1")
                .attribute("method", "POST").attribute("body", "{\n" + //
                        "    \"id\": 1,\n" + //
                        "    \"username\": \"vepo\"\n" + //
                        "}")
                .assertion(new Assertion<>("responseCode", "Equals", 200L)).build()).build(),
                parseSuite("Suite T1 {\n" + //
                        "    HTTP Step1 {\n" + //
                        "        method  : \"POST\"\n" + //
                        "        body    : \"\"\"\n" + //
                        "                  {\n" + //
                        "                      \"id\": 1,\n" + //
                        "                      \"username\": \"vepo\"\n" + //
                        "                  }\n" + //
                        "                  \"\"\"\n" + //
                        "        assert responseCode Equals 200\n" + //
                        "    }\n" + //
                        "}"));
    }

    @Test
    @DisplayName("It SHOULD parse Step assertion")
    public void stepWithAssertionTest() {
        assertEquals(Suite.builder().index(0).name("T1")
                .child(Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
                        .attribute("timeout", 1000L).attribute("minValue", -5L).attribute("maxValue", 1500000000L)
                        .attribute("ignoreSomething", true).assertion(new Assertion<>("responseCode", "Equals", 200L))
                        .assertion(new Assertion<>("responseMessage", "Equals", "OK"))
                        .assertion(new Assertion<>("body", "Equals", null)).build())
                .build(),
                parseSuite("Suite T1 {\n" + //
                        "    HTTP Step1 {\n" + //
                        "        method  : \"GET\"\n" + //
                        "        timeout : 1000\n" + //
                        "        minValue: -5\n" + //
                        "        maxValue: 1500000000\n" + //
                        "        ignoreSomething: true\n" + //
                        "        assert responseCode    Equals 200\n" + //
                        "        assert responseMessage Equals \"OK\"\n" + //
                        "        assert body Equals null\n" + //
                        "    }\n" + //
                        "}"));
    }

    @Test
    @DisplayName("It SHOULD parse a Suite with a Plugin")
    public void suiteWithPluginParseTest() {
        assertEquals(Suite.builder().index(0).name("T1")
                .child(Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
                        .attribute("timeout", 1000L).attribute("minValue", -5L).attribute("maxValue", 1500000000L)
                        .build())
                .build(),
                parseSuite("Suite T1 {\n" + //
                        "    HTTP Step1 {\n" + //
                        "        method: \"GET\"\n" + //
                        "        timeout: 1000\n" + //
                        "        minValue: -5\n" + //
                        "        maxValue: 1500000000\n" + //
                        "    }\n" + //
                        "}"));
    }

    @Nested
    @DisplayName("Properties")
    public class PropertiesTest {
        @Test
        @DisplayName("It should allow Properties")
        public void parsePropertiesTest() {
            Suite suite = parseSuite("Suite T1 {\n" + //
                    "    Properties {\n" + //
                    "        command: \"ls\"\n" + //
                    "        timeout: 150\n" + //
                    "    }\n" + //
                    "    CMD Step1 {\n" + //
                    "        cmd: ${command}\n" + //
                    "        timeout: ${timeout}\n" + //
                    "    }\n" + //
                    "}");
            assertEquals(Suite.builder().index(0).name("T1")
                    .child(Properties.builder().index(0).value("command", "ls").value("timeout", 150L).build())
                    .child(Step.builder().index(1).name("Step1").plugin("CMD")
                            .attribute("cmd", new PropertyReference("command"))
                            .attribute("timeout", new PropertyReference("timeout")).build())
                    .build(), suite);

            Step command = (Step) suite.getChild(1);
            assertEquals("ls", command.requiredAttribute("cmd", String.class));
            assertEquals(150L, command.requiredAttribute("timeout", Long.class));
        }

        @Test
        @DisplayName("It should allow Properties in inner Suites")
        public void parsePropertiesInnerSuiteTest() {
            Suite suite = parseSuite("Suite T1 {\n" + //
                    "    Properties {\n" + //
                    "        command: \"ls\"\n" + //
                    "        timeout: 150\n" + //
                    "        otherValue: \"xxxx\"\n" + //
                    "    }\n" + //
                    "    CMD Step1 {\n" + //
                    "        cmd: ${command}\n" + //
                    "        timeout: ${timeout}\n" + //
                    "    }\n" + //
                    "    Suite T2 {\n" + //
                    "        Properties {\n" + //
                    "            command: \"dir\"\n" + //
                    "            timeout: 50\n" + //
                    "        }\n" + //
                    "        CMD Step2 {\n" + //
                    "            cmd: ${command}\n" + //
                    "            timeout: ${timeout}\n" + //
                    "            otherValue: ${otherValue}\n" + //
                    "            notDefined: ${notDefined}\n" + //
                    "            replace: \"The ${command} should be \\\"dir\\\", ${otherValue} should be \\\"xxxx\\\". But ${yyy} should remain!\"\n"
                    + //
                    "        }\n" + //
                    "    }" + //
                    "}");
            assertEquals(Suite.builder().index(0).name("T1")
                    .child(Properties.builder().index(0).value("command", "ls").value("timeout", 150L)
                            .value("otherValue", "xxxx").build())
                    .child(Step.builder().index(1).name("Step1").plugin("CMD")
                            .attribute("cmd", new PropertyReference("command"))
                            .attribute("timeout", new PropertyReference("timeout")).build())
                    .child(Suite.builder().index(2).name("T2")
                            .child(Properties.builder().index(0).value("command", "dir").value("timeout", 50L).build())
                            .child(Step.builder().index(1).name("Step2").plugin("CMD")
                                    .attribute("cmd", new PropertyReference("command"))
                                    .attribute("timeout", new PropertyReference("timeout"))
                                    .attribute("otherValue", new PropertyReference("otherValue"))
                                    .attribute("notDefined", new PropertyReference("notDefined"))
                                    .attribute("replace",
                                            "The ${command} should be \"dir\", ${otherValue} should be \"xxxx\". But ${yyy} should remain!")
                                    .build())
                            .build())
                    .build(), suite);

            Step step1 = (Step) suite.getChild(1);
            assertEquals("ls", step1.requiredAttribute("cmd", String.class));
            assertEquals(150L, step1.requiredAttribute("timeout", Long.class));

            Step step2 = (Step) ((Suite) suite.getChild(2)).getChild(1);
            assertEquals("dir", step2.requiredAttribute("cmd", String.class));
            assertEquals(50L, step2.requiredAttribute("timeout", Long.class));
            assertEquals("xxxx", step2.requiredAttribute("otherValue", String.class));
            assertThrows(PropertyNotDefinedException.class, () -> step2.requiredAttribute("notDefined", String.class));
            assertEquals("The dir should be \"dir\", xxxx should be \"xxxx\". But ${yyy} should remain!",
                    step2.requiredAttribute("replace", String.class));
        }
    }

    @Nested
    @DisplayName("Step")
    public class StepTest {
        @Test
        @DisplayName("It SHOULD allow access attributes")
        public void attributeTest() {
            Step step = Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
                    .attribute("timeout", 1000L).attribute("minValue", -5L).attribute("maxValue", 1500000000L).build();
            assertEquals(-5L, step.requiredAttribute("minValue", Long.class));

            assertFalse(step.hasAttribute("notFoundAttribute"));
            assertThrows(IllegalStateException.class, () -> step.requiredAttribute("notFoundAttribute", String.class));

            assertThat(step.optionalAttribute("notFoundAttribute", String.class)).isEmpty();
            assertThat(step.optionalAttribute("minValue", Long.class)).isNotEmpty().hasValue(-5L);
        }

        @Test
        @DisplayName("It SHOULD allow access attributes from external")
        public void attributeWithExternalPropertiesTest() {
            Step step = Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
                    .attribute("xyz", new PropertyReference("property1")).attribute("timeout", 1000L)
                    .attribute("minValue", -5L).attribute("maxValue", 1500000000L).build();
            assertEquals(-5L, step.requiredAttribute("minValue", Long.class));

            assertFalse(step.hasAttribute("notFoundAttribute"));
            assertThrows(IllegalStateException.class, () -> step.requiredAttribute("notFoundAttribute", String.class));

            assertThat(step.optionalAttribute("notFoundAttribute", String.class)).isEmpty();
            assertThat(step.optionalAttribute("minValue", Long.class)).isNotEmpty().hasValue(-5L);

            step.setPropertiesResolver(new PropertiesResolver() {

                @SuppressWarnings("unchecked")
                @Override
                public <T> T findRequiredPropertyValue(String key) {
                    return (T) ("xyz.value");
                }

                @SuppressWarnings("unchecked")
                @Override
                public <T> Optional<T> findOptionalPropertyValue(String key) {
                    return Optional.of((T) ("xyz.value"));
                }
            });

            assertThat(step.requiredAttribute("xyz", String.class)).isEqualTo("xyz.value");
            assertThat(step.optionalAttribute("xyz", String.class)).isNotEmpty().hasValue("xyz.value");

            step.setPropertiesResolver(new PropertiesResolver() {

                @Override
                public <T> T findRequiredPropertyValue(String key) {
                    throw new IllegalStateException();
                }

                @Override
                public <T> Optional<T> findOptionalPropertyValue(String key) {
                    return Optional.empty();
                }
            });
            assertThat(step.optionalAttribute("xyz", String.class)).isEmpty();
        }

        @Test
        @DisplayName("It SHOULD allow access attributes from external from parent")
        public void attributeWithExternalPropertiesFromParentTest() {
            SuiteBuilder mainBuilder = Suite.builder().index(0).name("Main");
            SuiteBuilder firstSuiteBuilder = Suite.builder().index(0).name("SubMain").parent(mainBuilder);

            Step step = Step.builder().index(0).plugin("HTTP").name("Step1").attribute("method", "GET")
                    .attribute("xyz", new PropertyReference("property1")).attribute("timeout", 1000L)
                    .attribute("minValue", -5L).attribute("maxValue", 1500000000L).parent(firstSuiteBuilder).build();
            Suite main = mainBuilder.child(firstSuiteBuilder.child(step).build()).build();
            Suite first = firstSuiteBuilder.build();

            assertEquals(-5L, step.requiredAttribute("minValue", Long.class));

            assertFalse(step.hasAttribute("notFoundAttribute"));
            assertThrows(IllegalStateException.class, () -> step.requiredAttribute("notFoundAttribute", String.class));

            assertThat(step.optionalAttribute("notFoundAttribute", String.class)).isEmpty();
            assertThat(step.optionalAttribute("minValue", Long.class)).isNotEmpty().hasValue(-5L);

            PropertiesResolver propertiesResolver = new PropertiesResolver() {

                @SuppressWarnings("unchecked")
                @Override
                public <T> T findRequiredPropertyValue(String key) {
                    return (T) ("xyz.value");
                }

                @SuppressWarnings("unchecked")
                @Override
                public <T> Optional<T> findOptionalPropertyValue(String key) {
                    return Optional.of((T) ("xyz.value"));
                }
            };
            main.setPropertiesResolver(propertiesResolver);

            assertThat(step.requiredAttribute("xyz", String.class)).isEqualTo("xyz.value");
            assertThat(step.optionalAttribute("xyz", String.class)).isNotEmpty().hasValue("xyz.value");

            main.setPropertiesResolver(null);
            first.setPropertiesResolver(propertiesResolver);
            assertThat(step.requiredAttribute("xyz", String.class)).isEqualTo("xyz.value");
            assertThat(step.optionalAttribute("xyz", String.class)).isNotEmpty().hasValue("xyz.value");

            first.setPropertiesResolver(null);
            main.setPropertiesResolver(new PropertiesResolver() {

                @Override
                public <T> T findRequiredPropertyValue(String key) {
                    throw new IllegalStateException();
                }

                @Override
                public <T> Optional<T> findOptionalPropertyValue(String key) {
                    return Optional.empty();
                }
            });
            assertThat(step.optionalAttribute("xyz", String.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multi Thread")
    public class MultiThreadTest {

        @Test
        @DisplayName("It should accept times")
        public void timesTest() {
            Suite suite = parseSuite("Suite UserTest {\r\n" +
                    "    times: 100\r\n" +
                    "    HTTP CreateUser {\r\n" +
                    "        url   : \"http://127.0.0.1\"\r\n" +
                    "        method: \"POST\"\r\n" +
                    "        body  : \"\"\"\r\n" +
                    "              {\r\n" +
                    "                  \"username\": \"${username[${index}]}\",\r\n" +
                    "                  \"firstName\": \"${firstName[${index}]}\",\r\n" +
                    "                  \"lastName\": \"${lastName[${index}]}\",\r\n" +
                    "                  \"age\": ${age[${index}]}\r\n" +
                    "              }\r\n" +
                    "              \"\"\"\r\n" +
                    "        assert responseCode Equals 201\r\n" +
                    "    }\r\n" +
                    "}");

            assertThat(suite)
                    .isEqualTo(Suite.builder().name("UserTest")
                            .times(100)
                            .child(Step.builder()
                                    .index(0)
                                    .name("CreateUser")
                                    .plugin("HTTP")
                                    .attribute("url", "http://127.0.0.1")
                                    .attribute("method", "POST")
                                    .attribute("body", "{\n" +
                                            "    \"username\": \"${username[${index}]}\",\n" +
                                            "    \"firstName\": \"${firstName[${index}]}\",\n" +
                                            "    \"lastName\": \"${lastName[${index}]}\",\n" +
                                            "    \"age\": ${age[${index}]}\n" +
                                            "}")
                                    .assertion(new Assertion<>("responseCode", "Equals", 201L))
                                    .build())
                            .build());
        }

        @Test
        @DisplayName("It should accept CSV Input")
        public void fromCsvTest() {
            Suite suite = parseSuite("Suite UserTest {\r\n" +
                    "    PropertiesSource {\r\n" +
                    "        type: CSV\r\n" +
                    "        file: ./users.csv\r\n" +
                    "        separator: \",\"\r\n" +
                    "        headers: username, firstName, lastName, age\r\n" +
                    "    }\r\n" +
                    "    HTTP CreateUser {\r\n" +
                    "        url   : \"http://127.0.0.1\"\r\n" +
                    "        method: \"POST\"\r\n" +
                    "        body  : \"\"\"\r\n" +
                    "              {\r\n" +
                    "                  \"username\": \"${username}\",\r\n" +
                    "                  \"firstName\": \"${firstName}\",\r\n" +
                    "                  \"lastName\": \"${lastName}\",\r\n" +
                    "                  \"age\": ${age}\r\n" +
                    "              }\r\n" +
                    "              \"\"\"\r\n" +
                    "        assert responseCode Equals 201\r\n" +
                    "    }\r\n" +
                    "}");

            assertThat(suite)
                    .isEqualTo(Suite.builder().name("UserTest")
                            .child(PropertiesSource.builder()
                                    .index(0)
                                    .type(SourceType.CSV)
                                    .file(Paths.get(".", "users.csv").toFile())
                                    .separator(",")
                                    .headers(asList("username", "firstName", "lastName", "age"))
                                    .build())
                            .child(Step.builder()
                                    .index(1)
                                    .name("CreateUser")
                                    .plugin("HTTP")
                                    .attribute("url", "http://127.0.0.1")
                                    .attribute("method", "POST")
                                    .attribute("body", "{\n" +
                                            "    \"username\": \"${username}\",\n" +
                                            "    \"firstName\": \"${firstName}\",\n" +
                                            "    \"lastName\": \"${lastName}\",\n" +
                                            "    \"age\": ${age}\n" +
                                            "}")
                                    .assertion(new Assertion<>("responseCode", "Equals", 201L))
                                    .build())
                            .build());
        }
    }
}
