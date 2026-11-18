package net.powergrid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerGrid implements ModInitializer {
	public static final String MOD_ID = "powergrid";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
			RegistryKey.of(RegistryKeys.ITEM_GROUP, id("main"));

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.init();
		ModItems.init();

		Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, FabricItemGroup.builder()
				.icon(() -> new ItemStack(ModBlocks.COAL_GENERATOR))
				.displayName(Text.translatable("itemGroup.powergrid"))
				.entries((context, entries) -> {
					entries.add(ModBlocks.COAL_GENERATOR);
					entries.add(ModBlocks.SOLAR_PANEL);
					entries.add(ModBlocks.BATTERY);
					entries.add(ModBlocks.COPPER_CABLE);
					entries.add(ModBlocks.POWER_CABLE);
					entries.add(ModBlocks.ELECTRIC_LAMP);
					entries.add(ModItems.MULTIMETER);
				})
				.build());

		LOGGER.info("PowerGrid: энергосеть запущена");
	}
}
