package com.mcmoddev.mineralogy.api;

/** Shapes supported by Mineralogy's bounded ore generator. */
public enum OrePattern {
	VEIN("vein"),
	CLUSTER("cluster"),
	CLOUD("cloud");

	private final String configName;

	OrePattern(String configName) {
		this.configName = configName;
	}

	public String configName() {
		return configName;
	}
}
