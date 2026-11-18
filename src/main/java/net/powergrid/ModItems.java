package net.powergrid;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.powergrid.item.MultimeterItem;

public final class ModItems {
	private ModItems() {}

	public static final Item MULTIMETER = Registry.register(Registries.ITEM, PowerGrid.id("multimeter"),
			new MultimeterItem(new Item.Settings().maxCount(1)));

	public static void init() {}
}
