package io.vepo.plaintest.runner.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Deque;
import java.util.LinkedList;

public class MockOs {
	private static Deque<String> oldOs = new LinkedList<>();

	private MockOs() {
	}

	public static void mockUnix() {
		try {
			oldOs.addLast(System.getProperty("os.name"));
			setFinalStatic(Os.class.getDeclaredField("os"), null);
			System.setProperty("os.name", "nix'");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void clear() {
		try {
			System.setProperty("os.name", oldOs.removeLast());
			setFinalStatic(Os.class.getDeclaredField("os"), null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);
		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		} catch (Throwable re) {
			// just ignore
		}
		field.set(null, newValue);
	}
}
