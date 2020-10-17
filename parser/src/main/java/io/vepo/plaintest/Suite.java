package io.vepo.plaintest;

import static java.util.Comparator.comparingInt;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Suite extends NamedTestItem {
    public static final class SuiteBuilder {
        private int index;
        private String name;
        private List<TestItem> children;
        private Path executionPath;

        private SuiteBuilder() {
            children = new ArrayList<>();
        }

        public SuiteBuilder index(int index) {
            this.index = index;
            return this;
        }

        public SuiteBuilder executionPath(Path executionPath) {
            this.executionPath = executionPath;
            return this;
        }

        public SuiteBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SuiteBuilder child(TestItem child) {
            children.add(child);
            return this;
        }

        public SuiteBuilder children(List<TestItem> children) {
            this.children.addAll(children);
            return this;
        }

        public Suite build() {
            return new Suite(this);
        }
    }

    public static final SuiteBuilder builder() {
        return new SuiteBuilder();
    }

    private final Path executionPath;
    private final List<TestItem> children;

    private Suite(SuiteBuilder builder) {
        super(builder.index, builder.name);
        children = builder.children;
        executionPath = builder.executionPath;
        children.forEach(child -> child.setParent(this));
    }

    public Path getExecutionPath() {
        return executionPath;
    }

    public List<TestItem> getChildren() {
        return children;
    }

    @Override
    public TestItem getChild(int index) {
        return children.get(index);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(executionPath).append(children)
                .hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Suite other = (Suite) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(executionPath, other.executionPath)
                .append(children, other.children).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("executionPath", executionPath).append("children", children).toString();
    }

    public void forEachOrdered(Consumer<Suite> suiteConsumer, Consumer<Step> stepConsumer) {
        children.stream().sorted(comparingInt(TestItem::getIndex)).forEachOrdered(child -> {
            if (child instanceof Step) {
                stepConsumer.accept((Step) child);
            } else if (child instanceof Suite) {
                suiteConsumer.accept((Suite) child);
            }
        });
    }

    public void forEachOrdered(Consumer<Suite> suiteConsumer, Consumer<Step> stepConsumer,
            Consumer<Properties> propertiesConsumer) {
        children.stream().sorted(comparingInt(TestItem::getIndex)).forEachOrdered(child -> {
            if (child instanceof Step) {
                stepConsumer.accept((Step) child);
            } else if (child instanceof Suite) {
                suiteConsumer.accept((Suite) child);
            } else if (child instanceof Properties) {
                propertiesConsumer.accept((Properties) child);
            }
        });
    }
}