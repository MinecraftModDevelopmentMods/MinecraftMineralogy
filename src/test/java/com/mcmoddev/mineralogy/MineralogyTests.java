package com.mcmoddev.mineralogy;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


class MineralogyTests {

	private static String MODID;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		MODID = Mineralogy.MODID;
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

//	@Test
//	void testAddItemReturnsItemUnlocalisedName() {
//		String name = "TestItem";
//				
//		Item item = new Item();
//		
//		item = Mineralogy.registerItem(item, name);
//		
//		assertEquals("item." + MODID  + "." + name, item.getUnlocalizedName());
//	}
//	
//	@Test
//	void testAddItemBlockReturnsBlockUnlocalisedName() {
//		Rock block = mock(Rock.class);
//
//		when(block.getUnlocalizedName()).thenReturn(MODID + ".TestBlock");
//				
//		Item item = new Item();
//		
//		item = Mineralogy.registerItem(new ItemBlock(block), "TestItem");
//		
//		assertEquals(MODID + ".TestBlock", item.getUnlocalizedName());
//	}
}
