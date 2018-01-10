package com.mcmoddev.lib.interfaces;

public interface IDynamicTabProvider extends ITabProvider {
	/**
	 * Add a new tab to the set
	 * @param tabName Name of tab to add
	 * @param searchable Is the tab searchable
	 * @param modID Id of the mod related to the tab
	 */
	void addTab(String tabName, boolean searchable, String modID);
	
	/**
	 * instructs the tab provider that tabs will be created in a batch process
	 */
	void initialisePostemptiveTabGeneration();
	
	/**
	 * instructs the tab provider to assign and create tabs in a batch process
	 */
	void executePostemptiveTabGeneration();
}
