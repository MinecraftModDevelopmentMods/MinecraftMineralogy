package com.mcmoddev.mineralogy.worldgen;

import java.util.Locale;

/** Shapes supported by Mineralogy's allocation-free managed ore feature. */
public enum OrePattern {
	VEIN("vein"),
	CLUSTER("cluster"),
	CLOUD("cloud");

	public final String configName;

	OrePattern(String configName) {
		this.configName = configName;
	}

	public static OrePattern fromConfigName(String value) {
		String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
		for (OrePattern pattern : values()) {
			if (pattern.configName.equals(normalized)) {
				return pattern;
			}
		}
		throw new IllegalArgumentException("Unknown ore pattern: " + value);
	}
}
