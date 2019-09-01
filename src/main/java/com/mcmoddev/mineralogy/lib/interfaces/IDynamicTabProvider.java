package com.mcmoddev.mineralogy.lib.interfaces;

/**
 * Defines a tab provider which is capable of adding new tabs
 * @author SkyBlade1978
 *
 */
public interface IDynamicTabProvider extends ITabProvider {
	/**
	 * Add a new tab to the set
	 * @param tabName Name of tab to add
	 * @param searchable Is the tab searchable
	 * @param modID Id of the mod related to the tab
	 * @return this
	 */
	IDynamicTabProvider addTab(String tabName, boolean searchable, String modID);
	
	/**
	 * The default logic used when creating new tabs
	 * @param generationMode
	 * @return this
	 */
	IDynamicTabProvider setDefaultTabCreationLogic(DefaultTabGenerationMode generationMode);
	
	/**
	 * Default behaviour of tab provider when adding tabs
	 * @author SkyBlade1978
	 *
	 */
	public enum DefaultTabGenerationMode {
		ByClass,
		ByMod
	}
	
	/**
	 * causes the tab icons to be reset/set
	 * @return this
	 */
	public IDynamicTabProvider setTabIcons();
}
