package com.mcmoddev.mineralogy.data;

import java.util.ArrayList;
import com.mcmoddev.mineralogy.RockType;

public class MaterialData {
	public static final Material ANDESITE = new Material("Andesite", RockType.IGNEOUS, 1.5, 10, 0, true);
	public static final Material BASALT = new Material("Basalt", RockType.IGNEOUS, 5, 100, 2, true);
	public static final Material DIORITE = new Material("Diorite", RockType.IGNEOUS, 1.5, 10, 0, true);
	public static final Material GRANITE = new Material("Granite", RockType.IGNEOUS, 3, 15, 1, true);
	public static final Material RHYOLITE = new Material("Rhyolite", RockType.IGNEOUS, 1.5, 10, 0, true);
	public static final Material PEGMATITE = new Material("Pegmatite", RockType.IGNEOUS, 1.5, 10, 0, true);
	public static final Material DIABASE = new Material("Diabase", RockType.IGNEOUS, 5, 100, 2, true);
	public static final Material GABBRO = new Material("Gabbro", RockType.IGNEOUS, 5, 100, 2, true);
	public static final Material PERIDOTITE = new Material("Peridotite", RockType.IGNEOUS, 3, 15, 0, true);
	public static final Material BASALTIC_GLASS = new Material("Basaltic_glass", RockType.IGNEOUS, 3, 15, 0, true);
	public static final Material SCORIA = new Material("Scoria", RockType.IGNEOUS, 1, 7, 0, true);
	public static final Material TUFF = new Material("Tuff", RockType.IGNEOUS, 2, 10, 0, true);
	
	public static final Material SHALE = new Material("Shale", RockType.SEDIMENTARY, 1.5, 10, 0, true);
	public static final Material CONGLOMERATE = new Material("Conglomerate", RockType.SEDIMENTARY, 1.5, 10, 0, true);
	public static final Material DOLOMITE = new Material("Dolomite", RockType.SEDIMENTARY, 3, 15, 1, true);
	public static final Material LIMESTONE = new Material("Limestone", RockType.SEDIMENTARY, 1.5, 10, 0, true);
	public static final Material MARBLE = new Material("Marble", RockType.SEDIMENTARY, 1.5, 10, 0, true);	
	public static final Material SILTSTONE = new Material("Siltstone", RockType.SEDIMENTARY, 1, 10, 0, true);
	public static final Material ROCK_SALT = new Material("Rock_salt", RockType.SEDIMENTARY, 1.5, 10, 0, true, false);

	public static final Material SLATE = new Material("Slate", RockType.METAMORPHIC, 1.5, 10, 0, true);
	public static final Material SCHIST = new Material("Schist", RockType.METAMORPHIC, 3, 15, 1, true);
	public static final Material GNEISS = new Material("Gneiss", RockType.METAMORPHIC, 3, 15, 1, true);
	public static final Material PHYLLITE = new Material("Phyllite", RockType.METAMORPHIC, 1.5, 10, 0, true);
	public static final Material AMPHIBOLITE = new Material("Amphibolite", RockType.METAMORPHIC, 3, 15, 1, true);
	public static final Material HORNFELS = new Material("Hornfels", RockType.METAMORPHIC, 3, 15, 1, true);
	public static final Material QUARTZITE = new Material("Quartzite", RockType.METAMORPHIC, 4, 15, 1, true);
	public static final Material NOVACULITE = new Material("Novaculite", RockType.METAMORPHIC, 3, 15, 1, true);
	
	
	public static ArrayList<Material> toArray() {
		ArrayList<Material> materialArray = new ArrayList<Material>();
		
		materialArray.add(MaterialData.ANDESITE); 
		materialArray.add(MaterialData.BASALT);
		materialArray.add(MaterialData.DIORITE);
		materialArray.add(MaterialData.GRANITE);
		materialArray.add(MaterialData.RHYOLITE); 
		materialArray.add(MaterialData.PEGMATITE);
		materialArray.add(MaterialData.DIABASE);
		materialArray.add(MaterialData.GABBRO);
		materialArray.add(MaterialData.PERIDOTITE);
		materialArray.add(MaterialData.BASALTIC_GLASS);
		materialArray.add(MaterialData.SCORIA);
		materialArray.add(MaterialData.TUFF);
		materialArray.add(MaterialData.SHALE); 
		materialArray.add(MaterialData.CONGLOMERATE);
		materialArray.add(MaterialData.DOLOMITE); 
		materialArray.add(MaterialData.LIMESTONE);
		materialArray.add(MaterialData.SILTSTONE);
		materialArray.add(MaterialData.MARBLE); 
		materialArray.add(MaterialData.SLATE);
		materialArray.add(MaterialData.SCHIST);
		materialArray.add(MaterialData.GNEISS);
		materialArray.add(MaterialData.PHYLLITE); 
		materialArray.add(MaterialData.AMPHIBOLITE);
		materialArray.add(MaterialData.HORNFELS);
		materialArray.add(MaterialData.QUARTZITE);
		materialArray.add(MaterialData.NOVACULITE);
		
		return materialArray;
	}
	
	private MaterialData() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
