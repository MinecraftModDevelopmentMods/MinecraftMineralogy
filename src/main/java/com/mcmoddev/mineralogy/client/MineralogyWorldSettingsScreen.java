package com.mcmoddev.mineralogy.client;

import java.util.Arrays;
import java.util.List;

import com.mcmoddev.mineralogy.MineralogyConfig;
import com.mcmoddev.mineralogy.MineralogyConfig.GeologyMode;
import com.mcmoddev.mineralogy.worldgen.FormationSettings.Preset;
import com.mcmoddev.mineralogy.worldgen.WorldGeologyProfile;
import com.mcmoddev.mineralogy.worldgen.WorldGeologyProfileManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

public final class MineralogyWorldSettingsScreen extends Screen {
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;

	private final Screen parent;
	private WorldGeologyProfile baseProfile;
	private GeologyMode geologyMode;
	private Preset horizontalSize;
	private Preset verticalThickness;
	private Preset waviness;
	private Preset edgeIrregularity;
	private Preset formationContinuity;
	private boolean placeCrudeOil;

	private CycleButton<GeologyMode> geologyModeButton;
	private CycleButton<Boolean> crudeOilButton;
	private CycleButton<Preset> horizontalSizeButton;
	private CycleButton<Preset> verticalThicknessButton;
	private CycleButton<Preset> wavinessButton;
	private CycleButton<Preset> edgeIrregularityButton;
	private CycleButton<Preset> formationContinuityButton;

	public MineralogyWorldSettingsScreen(Screen parent, WorldGeologyProfile profile) {
		super(new TranslatableComponent("screen.mineralogy.world_settings"));
		this.parent = parent;
		setProfile(profile);
	}

	@Override
	protected void init() {
		int left = this.width / 2 - 155;
		int right = this.width / 2 + 5;
		int top = 52;

		geologyModeButton = addRenderableWidget(CycleButton
				.builder(this::geologyModeName)
				.withValues(Arrays.asList(GeologyMode.GEOME, GeologyMode.LEGACY))
				.withInitialValue(geologyMode)
				.withTooltip(value -> tooltip("tooltip.mineralogy.geology_mode"))
				.create(left, top, BUTTON_WIDTH, BUTTON_HEIGHT,
						new TranslatableComponent("option.mineralogy.geology_mode"),
						(button, value) -> {
							geologyMode = value;
							updateFormationControls();
						}));

		crudeOilButton = addRenderableWidget(CycleButton.onOffBuilder(placeCrudeOil)
				.withTooltip(value -> tooltip("tooltip.mineralogy.crude_oil"))
				.create(right, top, BUTTON_WIDTH, BUTTON_HEIGHT,
						new TranslatableComponent("option.mineralogy.crude_oil"),
						(button, value) -> placeCrudeOil = value));

		horizontalSizeButton = addPresetButton(left, top + 28,
				"option.mineralogy.horizontal_size", "tooltip.mineralogy.horizontal_size",
				horizontalSize, value -> horizontalSize = value);
		verticalThicknessButton = addPresetButton(right, top + 28,
				"option.mineralogy.vertical_thickness", "tooltip.mineralogy.vertical_thickness",
				verticalThickness, value -> verticalThickness = value);
		wavinessButton = addPresetButton(left, top + 56,
				"option.mineralogy.waviness", "tooltip.mineralogy.waviness",
				waviness, value -> waviness = value);
		edgeIrregularityButton = addPresetButton(right, top + 56,
				"option.mineralogy.edge_irregularity", "tooltip.mineralogy.edge_irregularity",
				edgeIrregularity, value -> edgeIrregularity = value);
		formationContinuityButton = addPresetButton(left, top + 84,
				"option.mineralogy.formation_continuity", "tooltip.mineralogy.formation_continuity",
				formationContinuity, value -> formationContinuity = value);

		addRenderableWidget(new Button(right, top + 84, BUTTON_WIDTH, BUTTON_HEIGHT,
				new TranslatableComponent("button.mineralogy.recommended"), button -> resetRecommended()));
		addRenderableWidget(new Button(left, this.height - 28, BUTTON_WIDTH, BUTTON_HEIGHT,
				CommonComponents.GUI_DONE, button -> saveAndClose()));
		addRenderableWidget(new Button(right, this.height - 28, BUTTON_WIDTH, BUTTON_HEIGHT,
				CommonComponents.GUI_CANCEL, button -> onClose()));
		updateFormationControls();
	}

	private CycleButton<Preset> addPresetButton(int x, int y, String labelKey, String tooltipKey,
			Preset initialValue, PresetConsumer consumer) {
		return addRenderableWidget(CycleButton.builder(this::presetName)
				.withValues(Arrays.asList(Preset.values()))
				.withInitialValue(initialValue)
				.withTooltip(value -> tooltip(tooltipKey))
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslatableComponent(labelKey),
						(button, value) -> consumer.accept(value)));
	}

	private void updateFormationControls() {
		boolean enabled = geologyMode == GeologyMode.GEOME;
		if (horizontalSizeButton != null) {
			horizontalSizeButton.active = enabled;
			verticalThicknessButton.active = enabled;
			wavinessButton.active = enabled;
			edgeIrregularityButton.active = enabled;
			formationContinuityButton.active = enabled;
		}
	}

	private void resetRecommended() {
		setProfile(WorldGeologyProfile.recommended(MineralogyConfig.placeCrudeOil()));
		geologyModeButton.setValue(geologyMode);
		crudeOilButton.setValue(placeCrudeOil);
		horizontalSizeButton.setValue(horizontalSize);
		verticalThicknessButton.setValue(verticalThickness);
		wavinessButton.setValue(waviness);
		edgeIrregularityButton.setValue(edgeIrregularity);
		formationContinuityButton.setValue(formationContinuity);
		updateFormationControls();
	}

	private void setProfile(WorldGeologyProfile profile) {
		baseProfile = profile;
		geologyMode = profile.geologyMode();
		horizontalSize = profile.horizontalSize();
		verticalThickness = profile.verticalThickness();
		waviness = profile.waviness();
		edgeIrregularity = profile.edgeIrregularity();
		formationContinuity = profile.formationContinuity();
		placeCrudeOil = profile.placeCrudeOil();
	}

	private void saveAndClose() {
		WorldGeologyProfileManager.setPendingNewWorldProfile(baseProfile.withSelection(
				geologyMode, horizontalSize, verticalThickness, waviness,
				edgeIrregularity, formationContinuity, placeCrudeOil));
		minecraft.setScreen(parent);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(poseStack);
		drawCenteredString(poseStack, font, title, width / 2, 20, 0xFFFFFF);
		super.render(poseStack, mouseX, mouseY, partialTick);
	}

	private Component geologyModeName(GeologyMode mode) {
		return new TranslatableComponent("value.mineralogy.geology_mode." + mode.name().toLowerCase(java.util.Locale.ROOT));
	}

	private Component presetName(Preset preset) {
		return new TranslatableComponent("value.mineralogy.preset." + preset.configName());
	}

	private List<FormattedCharSequence> tooltip(String key) {
		return font.split(new TranslatableComponent(key), 240);
	}

	@FunctionalInterface
	private interface PresetConsumer {
		void accept(Preset preset);
	}
}
