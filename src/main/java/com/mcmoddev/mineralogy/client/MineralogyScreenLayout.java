package com.mcmoddev.mineralogy.client;

/** Shared dimensions for the compact world-creation screens. */
final class MineralogyScreenLayout {
	private static final int COMPACT_HEIGHT = 260;

	private MineralogyScreenLayout() { }

	static int mainTop(int height) {
		return height < COMPACT_HEIGHT ? 28 : 42;
	}

	static int mainRowSpacing(int height) {
		return height < COMPACT_HEIGHT ? 22 : 24;
	}

	static int mainTitleY(int height) {
		return height < COMPACT_HEIGHT ? 7 : 16;
	}

	static int mainErrorY(int height) {
		return height < COMPACT_HEIGHT ? 18 : 30;
	}

	static int footerY(int height) {
		return height - 28;
	}
}
