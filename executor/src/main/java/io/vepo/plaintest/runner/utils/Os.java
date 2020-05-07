package io.vepo.plaintest.runner.utils;

import static java.util.Objects.isNull;

public class Os {
	// Operating systems.
	public enum OS {
		WINDOWS, LINUX, MAC, SOLARIS
	};

	private static OS os = null;

	public static OS getOS() {
		if (isNull(os)) {
			String operSys = System.getProperty("os.name").toLowerCase();
			if (operSys.contains("win")) {
				os = OS.WINDOWS;
			} else if (operSys.contains("nix") || operSys.contains("nux") || operSys.contains("aix")) {
				os = OS.LINUX;
			} else if (operSys.contains("mac")) {
				os = OS.MAC;
			} else if (operSys.contains("sunos")) {
				os = OS.SOLARIS;
			}
		}
		return os;
	}
}
