package io.vepo.plaintest.exceptions;

public class PropertyNotDefinedException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -7336352805350631806L;

	public PropertyNotDefinedException(String name) {
		super("Property not defined: " + name);
	}

}
