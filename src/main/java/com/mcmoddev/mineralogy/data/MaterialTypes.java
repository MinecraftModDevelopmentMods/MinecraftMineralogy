package com.mcmoddev.mineralogy.data;

import java.util.ArrayList;
import com.mcmoddev.mineralogy.RockType;

public class MaterialTypes {
	public static final MaterialType ANDESITE = new MaterialType("Andesite", RockType.IGNEOUS, 1.5, 10, 0);
	public static final MaterialType BASALT = new MaterialType("Basalt", RockType.IGNEOUS, 5, 100, 2);
	public static final MaterialType DIORITE = new MaterialType("Diorite", RockType.IGNEOUS, 1.5, 10, 0);
	public static final MaterialType GRANITE = new MaterialType("Granite", RockType.IGNEOUS, 3, 15, 1);
	public static final MaterialType RHYOLITE = new MaterialType("Rhyolite", RockType.IGNEOUS, 1.5, 10, 0);
	public static final MaterialType PEGMATITE = new MaterialType("Pegmatite", RockType.IGNEOUS, 1.5, 10, 0);

	public static final MaterialType SHALE = new MaterialType("Shale", RockType.SEDIMENTARY, 1.5, 10, 0);
	public static final MaterialType CONGLOMERATE = new MaterialType("Conglomerate", RockType.SEDIMENTARY, 1.5, 10, 0);
	public static final MaterialType DOLOMITE = new MaterialType("Dolomite", RockType.SEDIMENTARY, 3, 15, 1);
	public static final MaterialType LIMESTONE = new MaterialType("Limestone", RockType.SEDIMENTARY, 1.5, 10, 0);
	public static final MaterialType MARBLE = new MaterialType("Marble", RockType.SEDIMENTARY, 1.5, 10, 0);
	
	public static final MaterialType SLATE = new MaterialType("Slate", RockType.METAMORPHIC, 1.5, 10, 0);
	public static final MaterialType SCHIST = new MaterialType("Slate", RockType.METAMORPHIC, 3, 15, 1);
	public static final MaterialType GNEISS = new MaterialType("Gneiss", RockType.METAMORPHIC, 3, 15, 1);
	public static final MaterialType PHYLLITE = new MaterialType("Phyllite", RockType.METAMORPHIC, 1.5, 10, 0);
	public static final MaterialType AMPHIBOLITE = new MaterialType("Amphibolite", RockType.METAMORPHIC, 3, 15, 1);
	
	public static ArrayList<MaterialType> toArray() {
		ArrayList<MaterialType> materialArray = new ArrayList<MaterialType>();
		
		materialArray.add(MaterialTypes.ANDESITE); 
		materialArray.add(MaterialTypes.BASALT);
		materialArray.add(MaterialTypes.DIORITE);
		materialArray.add(MaterialTypes.GRANITE);
		materialArray.add(MaterialTypes.RHYOLITE); 
		materialArray.add(MaterialTypes.PEGMATITE);
		materialArray.add(MaterialTypes.SHALE); 
		materialArray.add(MaterialTypes.CONGLOMERATE);
		materialArray.add(MaterialTypes.DOLOMITE); 
		materialArray.add(MaterialTypes.LIMESTONE); 
		materialArray.add(MaterialTypes.MARBLE); 
		materialArray.add(MaterialTypes.SLATE);
		materialArray.add(MaterialTypes.SCHIST);
		materialArray.add(MaterialTypes.GNEISS);
		materialArray.add(MaterialTypes.PHYLLITE); 
		materialArray.add(MaterialTypes.AMPHIBOLITE);
		
		return materialArray;
	}
	
	private MaterialTypes() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
