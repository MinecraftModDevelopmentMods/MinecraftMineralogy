package com.mcmoddev.lib.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mcmoddev.lib.exceptions.ItemNotFoundException;
import com.mcmoddev.lib.exceptions.TabNotFoundException;
import com.mcmoddev.lib.interfaces.IDynamicTabProvider;
import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

class DynamicTabProviderTests {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
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

	@Test
	void testProviderCanBeInstansiated() {
		IDynamicTabProvider dynamicTabProvider = new DynamicTabProvider();
		
		assertNotNull(dynamicTabProvider);
	}

	@Test
	void testProviderCanAddTabAndItem() {
		MinIoC IoC = MinIoC.getInstance(false);
		IDynamicTabProvider dynamicTabProvider = new DynamicTabProvider();
	
		dynamicTabProvider.addTab("myNewTab", true, "myNewMod");
		
		int tabCount = dynamicTabProvider.getTabs().length;
		
		assertEquals(1, tabCount);
		
		Item item = mock(Item.class);
		
		try {
			dynamicTabProvider.addToTab(item);
		} catch (TabNotFoundException | ItemNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
