package com.mcmoddev.mineralogy.client;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcmoddev.mineralogy.worldgen.RockFamily;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

final class OreDimensionScreen extends Screen {
	private enum Page { PLACEMENT, HOSTS }

	private final Screen parent;
	private final GeologyEditorSession session;
	private final String oreId;
	private final String dimensionId;
	private final EnumSet<RockFamily> families = EnumSet.noneOf(RockFamily.class);
	private boolean enabled;
	private EditBox minY;
	private EditBox maxY;
	private EditBox frequency;
	private EditBox quantity;
	private EditBox hostBlocks;
	private EditBox hostTags;
	private Component error;
	private Page page = Page.PLACEMENT;
	private final List<AbstractWidget> placementWidgets = new ArrayList<>();
	private final List<AbstractWidget> hostWidgets = new ArrayList<>();

	OreDimensionScreen(Screen parent, GeologyEditorSession session, String oreId, String dimensionId) {
		super(new TranslatableComponent("screen.mineralogy.ore_dimension"));
		this.parent = parent;
		this.session = session;
		this.oreId = oreId;
		this.dimensionId = dimensionId;
		load();
	}

	private void load() {
		JsonObject rule = rule();
		enabled = GeologyEditorSession.bool(rule, "enabled", true);
		families.clear();
		if (rule.has("host_families") && rule.get("host_families").isJsonArray()) {
			for (JsonElement value : rule.getAsJsonArray("host_families")) {
				try { families.add(RockFamily.fromConfigName(value.getAsString())); }
				catch (RuntimeException ignored) { }
			}
		}
	}

	@Override
	protected void init() {
		JsonObject rule = rule();
		int left = width / 2 - 155;
		int right = width / 2 + 5;
		placementWidgets.clear();
		hostWidgets.clear();
		addRenderableWidget(new Button(left, 32, 100, 20,
				new TranslatableComponent("tab.mineralogy.placement"), button -> showPage(Page.PLACEMENT)));
		addRenderableWidget(new Button(left + 105, 32, 100, 20,
				new TranslatableComponent("tab.mineralogy.hosts"), button -> showPage(Page.HOSTS)));
		addRenderableWidget(CycleButton.onOffBuilder(enabled).create(left + 210, 32, 100, 20,
				new TranslatableComponent("option.mineralogy.enabled"), (button, value) -> enabled = value));
		minY = addPlacementField(right, 56, "min_y", text(rule, "min_y", -64));
		maxY = addPlacementField(right, 80, "max_y", text(rule, "max_y", 64));
		frequency = addPlacementField(right, 104, "frequency", text(rule, "frequency", 1.0D));
		quantity = addPlacementField(right, 128, "quantity", text(rule, "quantity", 8));

		RockFamily[] values = RockFamily.values();
		for (int i = 0; i < values.length; i++) {
			RockFamily family = values[i];
			int x = (i & 1) == 0 ? left : right;
			int y = 154 + ((i / 2) * 24);
			placementWidgets.add(addRenderableWidget(CycleButton.onOffBuilder(families.contains(family)).create(x, y, 150, 20,
					new TranslatableComponent("value.mineralogy.family." + family.configName),
					(button, selected) -> {
						if (selected) families.add(family); else families.remove(family);
					})));
		}

		hostBlocks = addHostField(left, 72, "host_blocks", join(rule.get("host_blocks")));
		hostTags = addHostField(left, 112, "host_tags", join(rule.get("host_tags")));
		Button weights = addRenderableWidget(new Button(left, 142, 150, 20,
				new TranslatableComponent("button.mineralogy.geome_weights"), button -> openWeights()));
		weights.active = "minecraft:overworld".equals(dimensionId);
		hostWidgets.add(weights);
		hostWidgets.add(addRenderableWidget(new Button(right, 142, 150, 20,
				new TranslatableComponent("button.mineralogy.remove_dimension"), button -> removeDimension())));

		int bottom = height - 28;
		addRenderableWidget(new Button(left, bottom, 150, 20, CommonComponents.GUI_DONE,
				button -> saveAndClose()));
		addRenderableWidget(new Button(right, bottom, 150, 20, CommonComponents.GUI_CANCEL,
				button -> onClose()));
		showPage(page);
	}

	private EditBox addPlacementField(int x, int y, String key, String value) {
		EditBox box = new EditBox(font, x, y, 150, 20, new TextComponent(key));
		box.setValue(value);
		box.setMaxLength(32);
		placementWidgets.add(addRenderableWidget(box));
		return box;
	}

	private EditBox addHostField(int x, int y, String key, String value) {
		EditBox box = new EditBox(font, x, y, 310, 20, new TextComponent(key));
		box.setValue(value);
		box.setMaxLength(1024);
		hostWidgets.add(addRenderableWidget(box));
		return box;
	}

	private void showPage(Page selected) {
		page = selected;
		for (AbstractWidget widget : placementWidgets) widget.visible = selected == Page.PLACEMENT;
		for (AbstractWidget widget : hostWidgets) widget.visible = selected == Page.HOSTS;
	}

	private void openWeights() {
		if (!save()) return;
		JsonObject rule = rule();
		JsonObject weights = rule.has("geomes") && rule.get("geomes").isJsonObject()
				? rule.getAsJsonObject("geomes") : new JsonObject();
		rule.add("geomes", weights);
		minecraft.setScreen(new WeightMapScreen(this, new TranslatableComponent("screen.mineralogy.geome_weights"),
				weights, session.geomeIds(), 1.0D));
	}

	private void saveAndClose() {
		if (save()) minecraft.setScreen(parent);
	}

	private boolean save() {
		try {
			int parsedMin = integer(minY, -2048, 2048);
			int parsedMax = integer(maxY, -2048, 2048);
			double parsedFrequency = number(frequency, 0.0D, 64.0D);
			int parsedQuantity = integer(quantity, 1, 64);
			if (parsedMin > parsedMax) throw new NumberFormatException();
			JsonArray blocks = ids(hostBlocks.getValue());
			JsonArray tags = ids(hostTags.getValue());
			if (enabled && families.isEmpty() && blocks.size() == 0 && tags.size() == 0) {
				error = new TextComponent("An enabled dimension needs at least one host.");
				return false;
			}
			JsonObject rule = rule();
			rule.addProperty("enabled", enabled);
			rule.addProperty("min_y", parsedMin);
			rule.addProperty("max_y", parsedMax);
			rule.addProperty("frequency", parsedFrequency);
			rule.addProperty("quantity", parsedQuantity);
			JsonArray familyArray = new JsonArray();
			for (RockFamily family : RockFamily.values()) {
				if (families.contains(family)) familyArray.add(family.configName);
			}
			rule.add("host_families", familyArray);
			rule.add("host_blocks", blocks);
			rule.add("host_tags", tags);
			error = null;
			return true;
		} catch (RuntimeException e) {
			error = new TextComponent("Check the numeric ranges and registry IDs.");
			return false;
		}
	}

	private void removeDimension() {
		JsonObject ore = session.ore(oreId);
		ore.getAsJsonObject("dimensions").remove(dimensionId);
		minecraft.setScreen(parent);
	}

	private JsonObject rule() {
		JsonObject ore = session.ore(oreId);
		if (!ore.has("dimensions") || !ore.get("dimensions").isJsonObject()) {
			ore.add("dimensions", new JsonObject());
		}
		JsonObject dimensions = ore.getAsJsonObject("dimensions");
		if (!dimensions.has(dimensionId) || !dimensions.get(dimensionId).isJsonObject()) {
			dimensions.add(dimensionId, GeologyEditorSession.defaultOreDimension());
		}
		return dimensions.getAsJsonObject(dimensionId);
	}

	private static JsonArray ids(String value) {
		JsonArray result = new JsonArray();
		for (String token : value.split(",")) {
			String id = token.trim();
			if (id.isEmpty()) continue;
			new ResourceLocation(id);
			result.add(id);
		}
		return result;
	}

	private static String join(JsonElement element) {
		if (element == null || !element.isJsonArray()) return "";
		StringBuilder result = new StringBuilder();
		for (JsonElement value : element.getAsJsonArray()) {
			if (result.length() > 0) result.append(", ");
			result.append(value.getAsString());
		}
		return result.toString();
	}

	private static String text(JsonObject json, String key, Number fallback) {
		return json.has(key) ? json.get(key).getAsString() : fallback.toString();
	}

	private static double number(EditBox box, double min, double max) {
		double value = Double.parseDouble(box.getValue().trim());
		if (!Double.isFinite(value) || value < min || value > max) throw new NumberFormatException();
		return value;
	}

	private static int integer(EditBox box, int min, int max) {
		double value = number(box, min, max);
		if (value != Math.rint(value)) throw new NumberFormatException();
		return (int) value;
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, 6, 0xFFFFFF);
		drawCenteredString(poseStack, font, new TextComponent(oreId + " / " + dimensionId),
				width / 2, 19, 0xDDDDDD);
		if (page == Page.PLACEMENT) {
			String[] labels = { "min_y", "max_y", "frequency", "quantity" };
			for (int i = 0; i < labels.length; i++) {
				drawString(poseStack, font, new TranslatableComponent("option.mineralogy." + labels[i]),
						width / 2 - 155, 62 + (i * 24), 0xDDDDDD);
			}
		} else {
			drawString(poseStack, font, new TranslatableComponent("option.mineralogy.host_blocks"),
					width / 2 - 155, 62, 0xDDDDDD);
			drawString(poseStack, font, new TranslatableComponent("option.mineralogy.host_tags"),
					width / 2 - 155, 102, 0xDDDDDD);
		}
		if (error != null) drawCenteredString(poseStack, font, error, width / 2, height - 40, 0xFF5555);
		super.render(poseStack, mouseX, mouseY, partialTick);
	}
}
