package com.mcmoddev.lib.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mcmoddev.lib.interfaces.IDynamicTabProvider;

import net.minecraft.profiler.ISnooperInfo;

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
	void testProviderCanAddTab() {
		IDynamicTabProvider dynamicTabProvider = new DynamicTabProvider();
		
		dynamicTabProvider.addTab("myNewTab", true, "myNewMod");
		
		int tabCount = dynamicTabProvider.getTabs().length;
		
		assertEquals(1, tabCount);
	}
}
