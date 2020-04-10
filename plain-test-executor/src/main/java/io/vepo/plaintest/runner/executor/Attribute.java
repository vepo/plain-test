package io.vepo.plaintest.runner.executor;

public class Attribute<T> {
	public final String key;
	public final Class<?> type;

	public Attribute(String key, Class<?> type) {
		this.key = key;
		this.type = type;
	}

	public String key() {
		return key;
	}

	public Class<?> type() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute<?> other = (Attribute<?>) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Attribute [key=" + key + ", type=" + type + "]";
	}

}
