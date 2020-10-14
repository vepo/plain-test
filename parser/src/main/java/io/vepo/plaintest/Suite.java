package io.vepo.plaintest;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Suite extends NamedSuiteChild {
    public static final class SuiteBuilder {
        private int index;
        private int times;
        private String name;
        private List<SuiteChild> children;
        private Map<SuiteAttributes, Object> attributes;
        private SuiteBuilder parent;
        private Suite instance;

        private SuiteBuilder() {
            times = 1;
            attributes = new HashMap<>();
            children = new ArrayList<>();
        }

        public SuiteBuilder index(int index) {
            this.index = index;
            return this;
        }

        public SuiteBuilder times(int times) {
            this.times = times;
            return this;
        }

        public SuiteBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SuiteBuilder child(SuiteChild child) {
            children.add(child);
            return this;
        }

        public SuiteBuilder attribute(SuiteAttributes key, Object value) {
            attributes.put(key, value);
            return this;
        }

        public SuiteBuilder parent(SuiteBuilder parent) {
            this.parent = parent;
            return this;
        }

        public Suite build() {
            if (isNull(instance)) {
                instance = new Suite(this);
            }
            return instance;
        }

        public int nextIndex() {
            return children.stream().mapToInt(SuiteChild::getIndex).max().orElse(-1) + 1;

        }
    }

    public static final SuiteBuilder builder() {
        return new SuiteBuilder();
    }

    private int times;
    private final List<SuiteChild> children;
    private final Map<SuiteAttributes, Object> attributes;

    private Suite(SuiteBuilder builder) {
        super(builder.index, builder.name, Optional.ofNullable(builder.parent).map(SuiteBuilder::build).orElse(null));
        times = builder.times;
        children = builder.children;
        attributes = builder.attributes;
    }

    public int getTimes() {
        return times;
    }

    public List<SuiteChild> getChildren() {
        return children;
    }

    public SuiteChild getChild(int index) {
        return children.get(index);
    }

    public Map<SuiteAttributes, Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> attribute(SuiteAttributes key) {
        if (attributes.containsKey(key)) {
            return Optional.of((T) attributes.get(key));
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> attribute(SuiteAttributes key, Class<T> requiredClass) {
        if (attributes.containsKey(key)) {
            Object value = attributes.get(key);
            if (value.getClass().isAssignableFrom(requiredClass)) {
                return Optional.of((T) value);
            } else {
                throw new IllegalStateException("Invalid type!");
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(times).append(attributes).append(children)
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
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(times, other.times)
                .append(attributes, other.attributes).append(children, other.children).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("times", times)
                .append("attributes", attributes).append("children", children).toString();
    }

    public void forEachOrdered(Consumer<Suite> suiteConsumer, Consumer<Step> stepConsumer) {
        children.stream().sorted(comparingInt(SuiteChild::getIndex)).forEachOrdered(child -> {
            if (child instanceof Step) {
                stepConsumer.accept((Step) child);
            } else if (child instanceof Suite) {
                suiteConsumer.accept((Suite) child);
            }
        });
    }

    public void forEachOrdered(Consumer<Suite> suiteConsumer, Consumer<Step> stepConsumer,
            Consumer<Properties> propertiesConsumer) {
        children.stream().sorted(comparingInt(SuiteChild::getIndex)).forEachOrdered(child -> {
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