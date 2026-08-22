package zone.moddev.mc.mineralogy.data;

import java.util.ArrayList;

public class MaterialData {
    public static final Material ANDESITE = new Material("Andesite", 1.5, 10, 0, true);
    public static final Material BASALT = new Material("Basalt", 5, 100, 2, true);
    public static final Material DIORITE = new Material("Diorite", 1.5, 10, 0, true);
    public static final Material GRANITE = new Material("Granite", 3, 15, 1, true);
    public static final Material RHYOLITE = new Material("Rhyolite", 1.5, 10, 0, true);
    public static final Material PEGMATITE = new Material("Pegmatite", 1.5, 10, 0, true);
    public static final Material DIABASE = new Material("Diabase", 5, 100, 2, true);
    public static final Material GABBRO = new Material("Gabbro", 5, 100, 2, true);
    public static final Material PERIDOTITE = new Material("Peridotite", 3, 15, 0, true);
    public static final Material BASALTIC_GLASS = new Material("Basaltic_glass", 3, 15, 0, true);
    public static final Material SCORIA = new Material("Scoria", 1, 7, 0, true);
    public static final Material TUFF = new Material("Tuff", 2, 10, 0, true);

    public static final Material SHALE = new Material("Shale", 1.5, 10, 0, true);
    public static final Material CONGLOMERATE = new Material("Conglomerate", 1.5, 10, 0, true);
    public static final Material DOLOMITE = new Material("Dolomite", 3, 15, 1, true);
    public static final Material LIMESTONE = new Material("Limestone", 1.5, 10, 0, true);
    public static final Material MARBLE = new Material("Marble", 1.5, 10, 0, true);
    public static final Material SILTSTONE = new Material("Siltstone", 1, 10, 0, true);
    public static final Material ROCK_SALT = new Material("Rock_salt", 1.5, 10, 0, true, false);

    public static final Material SLATE = new Material("Slate", 1.5, 10, 0, true);
    public static final Material SCHIST = new Material("Schist", 3, 15, 1, true);
    public static final Material GNEISS = new Material("Gneiss", 3, 15, 1, true);
    public static final Material PHYLLITE = new Material("Phyllite", 1.5, 10, 0, true);
    public static final Material AMPHIBOLITE = new Material("Amphibolite", 3, 15, 1, true);
    public static final Material HORNFELS = new Material("Hornfels", 3, 15, 1, true);
    public static final Material QUARTZITE = new Material("Quartzite", 4, 15, 1, true);
    public static final Material NOVACULITE = new Material("Novaculite", 3, 15, 1, true);


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
