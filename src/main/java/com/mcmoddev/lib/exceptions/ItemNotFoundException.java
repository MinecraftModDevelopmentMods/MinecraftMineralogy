package com.mcmoddev.lib.exceptions;

public class ItemNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ItemNotFoundException(String itemName) {
		super(itemName + " was not found or not mapped");
	}

}
