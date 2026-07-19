package com.arcpath;

import com.arcpath.config.ArcPathConfig;
import com.arcpath.config.ArcPathConfig.ThrowableSettings;
import com.arcpath.config.ArcStyle;
import com.arcpath.config.MarkerShape;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.shedaniel.math.Color;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

public class ArcPathModMenuEntry implements ModMenuApi {

	private static final MutableComponent TITLE_LABEL = Component.translatable("arcpath.config.title");
	private static final MutableComponent THROWABLES_LABEL = Component.translatable("arcpath.config.category.throwables");
	private static final MutableComponent ENDER_PEARL_LABEL = Component.translatable("arcpath.config.throwable.ender_pearl");
	private static final MutableComponent SNOWBALL_LABEL = Component.translatable("arcpath.config.throwable.snowball");
	private static final MutableComponent EGG_LABEL = Component.translatable("arcpath.config.throwable.egg");
	private static final MutableComponent TRIDENT_LABEL = Component.translatable("arcpath.config.throwable.trident");
	private static final MutableComponent BOW_LABEL = Component.translatable("arcpath.config.throwable.bow");
	private static final MutableComponent ENABLED_LABEL = Component.translatable("arcpath.config.enabled");
	private static final MutableComponent COLOR_LABEL = Component.translatable("arcpath.config.color");
	private static final MutableComponent LINE_WIDTH_LABEL = Component.translatable("arcpath.config.line_width");
	private static final MutableComponent TRANSPARENCY_LABEL = Component.translatable("arcpath.config.transparency");
	private static final MutableComponent ARC_LABEL = Component.translatable("arcpath.config.arc");
	private static final MutableComponent DASH_LENGTH_LABEL = Component.translatable("arcpath.config.arc.dash_length");
	private static final MutableComponent GAP_LENGTH_LABEL = Component.translatable("arcpath.config.arc.gap_length");
	private static final MutableComponent TARGET_LABEL = Component.translatable("arcpath.config.target");
	private static final MutableComponent RADIUS_LABEL = Component.translatable("arcpath.config.target.radius");
	private static final MutableComponent TARGET_SHAPE_LABEL = Component.translatable("arcpath.config.target.shape");
	private static final MutableComponent ARC_STYLE_LABEL = Component.translatable("arcpath.config.arc.style");
	private static final MutableComponent GRADIENT_ENABLED_LABEL = Component.translatable("arcpath.config.arc.gradient_enabled");
	private static final MutableComponent GRADIENT_COLOR_LABEL = Component.translatable("arcpath.config.gradient_color");

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ArcPathConfig config = ArcPathConfig.get();

			ConfigBuilder builder = ConfigBuilder.create()
					.setParentScreen(parent)
					.setTitle(TITLE_LABEL)
					.setSavingRunnable(config::save);

			ConfigEntryBuilder eb = builder.entryBuilder();
			ConfigCategory category = builder.getOrCreateCategory(THROWABLES_LABEL);

			category.addEntry(buildSubCategory(eb, ENDER_PEARL_LABEL.getString(), config.enderPearl,
					s -> ArcPathConfig.get().enderPearl = s));

			category.addEntry(buildSubCategory(eb, SNOWBALL_LABEL.getString(), config.snowball,
					s -> ArcPathConfig.get().snowball = s));

			category.addEntry(buildSubCategory(eb, EGG_LABEL.getString(), config.egg,
					s -> ArcPathConfig.get().egg = s));

			category.addEntry(buildSubCategory(eb, TRIDENT_LABEL.getString(), config.trident,
					s -> ArcPathConfig.get().trident = s));

			category.addEntry(buildSubCategory(eb, BOW_LABEL.getString(), config.arrow,
					s -> ArcPathConfig.get().arrow = s));

			return builder.build();
		};
	}

	private AbstractConfigListEntry<?> buildSubCategory(ConfigEntryBuilder eb, @NonNull String name, ThrowableSettings settings, Consumer<ThrowableSettings> onSave) {

		var sub = eb.startSubCategory(Component.translatable(name));

		sub.add(eb.startBooleanToggle(ENABLED_LABEL, settings.enabled)
				.setDefaultValue(true)
				.setSaveConsumer(val -> {
					settings.enabled = val;
					onSave.accept(settings);
				})
				.build());

		addArcEntries(eb, sub, settings, onSave);
		addTargetEntries(eb, sub, settings, onSave);

		return sub.build();
	}

	private void addArcEntries(ConfigEntryBuilder eb, SubCategoryBuilder sub, ThrowableSettings settings, Consumer<ThrowableSettings> onSave) {

		sub.add(eb.startTextDescription(ARC_LABEL).build());

		sub.add(eb.startBooleanToggle(ENABLED_LABEL, settings.arc.enabled)
				.setDefaultValue(true)
				.setSaveConsumer(val -> {
					settings.arc.enabled = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startEnumSelector(ARC_STYLE_LABEL, ArcStyle.class, settings.arc.style)
				.setDefaultValue(ArcStyle.DASHED)
				.setSaveConsumer(val -> { 
					settings.arc.style = val; 
					onSave.accept(settings); 
				})
				.build());

		sub.add(eb.startIntSlider(LINE_WIDTH_LABEL, settings.arc.lineWidth, 1, 20)
				.setDefaultValue(6)
				.setSaveConsumer(val -> {
					settings.arc.lineWidth = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startIntSlider(DASH_LENGTH_LABEL, settings.arc.dashLength, 1, 20)
				.setDefaultValue(3)
				.setSaveConsumer(val -> {
					settings.arc.dashLength = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startIntSlider(GAP_LENGTH_LABEL, settings.arc.gapLength, 0, 20)
				.setDefaultValue(3)
				.setSaveConsumer(val -> {
					settings.arc.gapLength = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startIntSlider(TRANSPARENCY_LABEL, settings.arc.transparency, 0, 100)
				.setDefaultValue(0)
				.setSaveConsumer(val -> {
					settings.arc.transparency = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startColorField(COLOR_LABEL, settings.arc.color.getColor() & 0xFFFFFF)
				.setDefaultValue(0xFFFFFF)
				.setSaveConsumer(val -> {
					settings.arc.color = Color.ofOpaque(val);
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startBooleanToggle(GRADIENT_ENABLED_LABEL, settings.arc.gradientEnabled)
				.setDefaultValue(false)
				.setSaveConsumer(val -> { 
					settings.arc.gradientEnabled = val; 
					onSave.accept(settings); 
				})
				.build());

		sub.add(eb.startColorField(GRADIENT_COLOR_LABEL, settings.arc.gradientColor.getColor() & 0xFFFFFF)
				.setDefaultValue(0xFF0000)
				.setSaveConsumer(val -> { 
					settings.arc.gradientColor = Color.ofOpaque(val); 
					onSave.accept(settings); 
				})
				.build());
	}

	private void addTargetEntries(ConfigEntryBuilder eb, SubCategoryBuilder sub, ThrowableSettings settings, Consumer<ThrowableSettings> onSave) {

		sub.add(eb.startTextDescription(TARGET_LABEL).build());

		sub.add(eb.startBooleanToggle(ENABLED_LABEL, settings.target.enabled)
				.setDefaultValue(true)
				.setSaveConsumer(val -> {
					settings.target.enabled = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startEnumSelector(TARGET_SHAPE_LABEL, MarkerShape.class, settings.target.shape)
				.setDefaultValue(MarkerShape.CIRCLE)
				.setSaveConsumer(val -> { 
					settings.target.shape = val; 
					onSave.accept(settings); 
				})
				.build());

		sub.add(eb.startColorField(COLOR_LABEL, settings.target.color.getColor() & 0xFFFFFF)
				.setDefaultValue(0xFFFFFF)
				.setSaveConsumer(val -> {
					settings.target.color = Color.ofOpaque(val);
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startIntSlider(LINE_WIDTH_LABEL, settings.target.lineWidth, 1, 20)
				.setDefaultValue(6)
				.setSaveConsumer(val -> {
					settings.target.lineWidth = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startIntSlider(RADIUS_LABEL, settings.target.targetSize, 1, 100)
				.setDefaultValue(20)
				.setSaveConsumer(val -> {
					settings.target.targetSize = val;
					onSave.accept(settings);
				})
				.build());

		sub.add(eb.startIntSlider(TRANSPARENCY_LABEL, settings.target.transparency, 0, 100)
				.setDefaultValue(0)
				.setSaveConsumer(val -> {
					settings.target.transparency = val;
					onSave.accept(settings);
				})
				.build());
	}
}