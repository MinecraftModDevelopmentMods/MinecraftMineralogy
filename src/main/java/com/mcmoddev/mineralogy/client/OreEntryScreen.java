package com.mcmoddev.mineralogy.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

final class OreEntryScreen extends Screen {
	private final Screen parent;
	private final GeologyEditorSession session;
	private final String oreId;
	private boolean enabled;
	private int page;
	private EditBox dimensionId;

	OreEntryScreen(Screen parent, GeologyEditorSession session, String oreId) {
		super(new TranslatableComponent("screen.mineralogy.ore_entry"));
		this.parent = parent;
		this.session = session;
		this.oreId = oreId;
		enabled = GeologyEditorSession.bool(session.ore(oreId), "enabled", true);
	}

	@Override
	protected void init() {
		JsonObject ore = session.ore(oreId);
		JsonObject dimensions = dimensions(ore);
		addRenderableWidget(CycleButton.onOffBuilder(enabled).create(width / 2 - 155, 48, 150, 20,
				new TranslatableComponent("option.mineralogy.enabled"), (button, value) -> {
					enabled = value; ore.addProperty("enabled", value);
				}));
		addRenderableWidget(new Button(width / 2 + 5, 48, 150, 20,
				new TranslatableComponent("button.mineralogy.reset"), button -> reset()));

		List<String> ids = new ArrayList<>(dimensions.keySet());
		Collections.sort(ids);
		int listTop = 76;
		int controlsY = height - 52;
		int pageSize = Math.max(1, (controlsY - listTop) / 24);
		int pageCount = Math.max(1, (ids.size() + pageSize - 1) / pageSize);
		page = Math.max(0, Math.min(page, pageCount - 1));
		int start = page * pageSize;
		for (int i = 0; i < pageSize && start + i < ids.size(); i++) {
			String id = ids.get(start + i);
			addRenderableWidget(new Button(width / 2 - 155, listTop + (i * 24), 310, 20,
					new TextComponent(id), button -> minecraft.setScreen(
							new OreDimensionScreen(this, session, oreId, id))));
		}
		Button previous = addRenderableWidget(new Button(width / 2 - 155, controlsY, 45, 20,
				new TextComponent("<"), button -> { page--; rebuildWidgets(); }));
		Button next = addRenderableWidget(new Button(width / 2 - 105, controlsY, 45, 20,
				new TextComponent(">"), button -> { page++; rebuildWidgets(); }));
		previous.active = page > 0;
		next.active = page + 1 < pageCount;
		dimensionId = addRenderableWidget(new EditBox(font, width / 2 - 55, controlsY, 150, 20,
				new TranslatableComponent("option.mineralogy.dimension")));
		dimensionId.setMaxLength(128);
		addRenderableWidget(new Button(width / 2 + 100, controlsY, 55, 20,
				new TranslatableComponent("button.mineralogy.add"), button -> addDimension()));

		int bottom = height - 28;
		addRenderableWidget(new Button(width / 2 - 155, bottom, 100, 20, CommonComponents.GUI_DONE,
				button -> onClose()));
		addRenderableWidget(new Button(width / 2 + 55, bottom, 100, 20,
				new TranslatableComponent("button.mineralogy.remove"), button -> unassign()));
	}

	private void addDimension() {
		String id = dimensionId.getValue().trim();
		try {
			new ResourceLocation(id);
		} catch (RuntimeException e) {
			return;
		}
		JsonObject dimensions = dimensions(session.ore(oreId));
		if (!dimensions.has(id)) {
			JsonObject rule;
			if ("minecraft:overworld".equals(id)) {
				rule = GeologyEditorSession.defaultOreDimension();
			} else {
				rule = new JsonObject();
				rule.addProperty("enabled", true);
				rule.addProperty("min_y", 0);
				rule.addProperty("max_y", 128);
				rule.addProperty("frequency", 1.0D);
				rule.addProperty("quantity", 8);
				JsonArray tags = new JsonArray();
				tags.add("minecraft:the_nether".equals(id)
						? "minecraft:base_stone_nether" : "minecraft:stone_ore_replaceables");
				rule.add("host_tags", tags);
			}
			dimensions.add(id, rule);
		}
		minecraft.setScreen(new OreDimensionScreen(this, session, oreId, id));
	}

	private void rebuildWidgets() {
		clearWidgets();
		init();
	}

	private void reset() {
		session.resetEntry("ores", oreId);
		enabled = GeologyEditorSession.bool(session.ore(oreId), "enabled", true);
		rebuildWidgets();
	}

	private void unassign() {
		session.disableOrRemoveOre(oreId);
		minecraft.setScreen(parent);
	}

	private static JsonObject dimensions(JsonObject ore) {
		if (!ore.has("dimensions") || !ore.get("dimensions").isJsonObject()) {
			JsonObject result = new JsonObject();
			ore.add("dimensions", result);
			return result;
		}
		return ore.getAsJsonObject("dimensions");
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, 10, 0xFFFFFF);
		drawCenteredString(poseStack, font, new TextComponent(oreId), width / 2, 25, 0xDDDDDD);
		String source = GeologyEditorSession.string(session.ore(oreId), "source_provider",
				GeologyEditorSession.string(session.ore(oreId), "source_mod", ""));
		if (!source.isEmpty()) {
			drawCenteredString(poseStack, font, new TextComponent("Source: " + source), width / 2, 36, 0xAAAAAA);
		}
		super.render(poseStack, mouseX, mouseY, partialTick);
	}
}
