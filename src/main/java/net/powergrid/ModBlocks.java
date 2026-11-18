package net.powergrid;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.powergrid.block.BatteryBlock;
import net.powergrid.block.CableBlock;
import net.powergrid.block.CoalGeneratorBlock;
import net.powergrid.block.ElectricLampBlock;
import net.powergrid.block.SolarPanelBlock;
import net.powergrid.blockentity.BatteryBlockEntity;
import net.powergrid.blockentity.CoalGeneratorBlockEntity;
import net.powergrid.blockentity.ElectricLampBlockEntity;
import net.powergrid.blockentity.SolarPanelBlockEntity;

public final class ModBlocks {
	private ModBlocks() {}

	// ---------- Кабели ----------
	/** Медный кабель: 64 EU/t. */
	public static final Block COPPER_CABLE = register("copper_cable", new CableBlock(64,
			AbstractBlock.Settings.create().strength(0.3f).sounds(BlockSoundGroup.WOOL).nonOpaque()));

	/** Силовой кабель: 512 EU/t. */
	public static final Block POWER_CABLE = register("power_cable", new CableBlock(512,
			AbstractBlock.Settings.create().strength(0.6f).sounds(BlockSoundGroup.WOOL).nonOpaque()));

	// ---------- Генерация ----------
	public static final Block COAL_GENERATOR = register("coal_generator", new CoalGeneratorBlock(
			AbstractBlock.Settings.create().strength(3.0f).sounds(BlockSoundGroup.METAL)
					.luminance(state -> state.get(Properties.LIT) ? 13 : 0)));

	public static final Block SOLAR_PANEL = register("solar_panel", new SolarPanelBlock(
			AbstractBlock.Settings.create().strength(1.5f).sounds(BlockSoundGroup.GLASS).nonOpaque()));

	// ---------- Накопление ----------
	public static final Block BATTERY = register("battery", new BatteryBlock(
			AbstractBlock.Settings.create().strength(3.0f).sounds(BlockSoundGroup.METAL)));

	// ---------- Потребители ----------
	public static final Block ELECTRIC_LAMP = register("electric_lamp", new ElectricLampBlock(
			AbstractBlock.Settings.create().strength(0.5f).sounds(BlockSoundGroup.GLASS)
					.luminance(state -> state.get(Properties.LIT) ? 15 : 0)));

	// ---------- Block entities ----------
	public static final BlockEntityType<CoalGeneratorBlockEntity> COAL_GENERATOR_BE = Registry.register(
			Registries.BLOCK_ENTITY_TYPE, PowerGrid.id("coal_generator"),
			FabricBlockEntityTypeBuilder.create(CoalGeneratorBlockEntity::new, COAL_GENERATOR).build());

	public static final BlockEntityType<SolarPanelBlockEntity> SOLAR_PANEL_BE = Registry.register(
			Registries.BLOCK_ENTITY_TYPE, PowerGrid.id("solar_panel"),
			FabricBlockEntityTypeBuilder.create(SolarPanelBlockEntity::new, SOLAR_PANEL).build());

	public static final BlockEntityType<BatteryBlockEntity> BATTERY_BE = Registry.register(
			Registries.BLOCK_ENTITY_TYPE, PowerGrid.id("battery"),
			FabricBlockEntityTypeBuilder.create(BatteryBlockEntity::new, BATTERY).build());

	public static final BlockEntityType<ElectricLampBlockEntity> ELECTRIC_LAMP_BE = Registry.register(
			Registries.BLOCK_ENTITY_TYPE, PowerGrid.id("electric_lamp"),
			FabricBlockEntityTypeBuilder.create(ElectricLampBlockEntity::new, ELECTRIC_LAMP).build());

	private static Block register(String name, Block block) {
		Registry.register(Registries.BLOCK, PowerGrid.id(name), block);
		Registry.register(Registries.ITEM, PowerGrid.id(name), new BlockItem(block, new Item.Settings()));
		return block;
	}

	/** Вызывается из PowerGrid.onInitialize(), чтобы загрузить класс и зарегистрировать всё выше. */
	public static void init() {}
}
