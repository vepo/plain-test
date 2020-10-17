package io.vepo.plaintest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Parallel extends TestItem {

    public static final class ParallelBuilder {
        private int index;
        private int times;
        private int maxThreads;
        private int rampUp;
        private List<TestItem> children;

        private ParallelBuilder() {
            children = new ArrayList<>();
        }

        public ParallelBuilder index(int index) {
            this.index = index;
            return this;
        }

        public ParallelBuilder times(int times) {
            this.times = times;
            return this;
        }

        public ParallelBuilder maxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public ParallelBuilder rampUp(int rampUp) {
            this.rampUp = rampUp;
            return this;
        }

        public ParallelBuilder child(TestItem child) {
            children.add(child);
            return this;
        }

        public ParallelBuilder children(List<TestItem> children) {
            this.children.addAll(children);
            return this;
        }

        public Parallel build() {
            return new Parallel(this);
        }
    }

    public static ParallelBuilder builder() {
        return new ParallelBuilder();
    }

    private final int times;
    private final int maxThreads;
    private final int rampUp;
    private final List<TestItem> children;

    private Parallel(ParallelBuilder builder) {
        super(builder.index);
        times = builder.times;
        maxThreads = builder.maxThreads;
        rampUp = builder.rampUp;
        children = builder.children;
        children.forEach(child -> child.setParent(this));
    }

    public int getTimes() {
        return times;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getRampUp() {
        return rampUp;
    }

    public List<TestItem> getChildren() {
        return children;
    }

    @Override
    protected TestItem getChild(int currIndex) {
        return children.get(currIndex);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(times).append(maxThreads).append(rampUp)
                .append(children).hashCode();
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
        Parallel other = (Parallel) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(times, other.times)
                .append(maxThreads, other.maxThreads).append(rampUp, other.rampUp).append(children, other.children)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("times", times).append("maxThreads", maxThreads).append("rampUp", rampUp)
                .append("children", children).toString();
    }

}
