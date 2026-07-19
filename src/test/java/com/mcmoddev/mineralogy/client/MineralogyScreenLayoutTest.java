package com.mcmoddev.mineralogy.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MineralogyScreenLayoutTest {
	@Test
	void compactMainRowsStayAboveFooter() {
		assertRowsClearFooter(240);
	}

	@Test
	void normalMainRowsStayAboveFooter() {
		assertRowsClearFooter(270);
	}

	private static void assertRowsClearFooter(int height) {
		int lastRowBottom = MineralogyScreenLayout.mainTop(height)
				+ (MineralogyScreenLayout.mainRowSpacing(height) * 7) + 20;
		assertTrue(lastRowBottom < MineralogyScreenLayout.footerY(height));
	}
}
