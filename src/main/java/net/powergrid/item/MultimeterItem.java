package net.powergrid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.block.GridNode;
import net.powergrid.grid.Grid;

/** Мультиметр: ПКМ по любому блоку сети — показать баланс мощности. */
public class MultimeterItem extends Item {
	public MultimeterItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		if (!(world.getBlockState(pos).getBlock() instanceof GridNode)) return ActionResult.PASS;
		if (world.isClient) return ActionResult.SUCCESS;

		PlayerEntity player = context.getPlayer();
		if (player == null) return ActionResult.PASS;

		Grid.Info info = Grid.scan(world, pos);
		int production = info.production();
		int demand = info.demand();
		int balance = production - demand;

		player.sendMessage(Text.translatable("message.powergrid.meter.header").formatted(Formatting.GOLD), false);
		player.sendMessage(Text.translatable("message.powergrid.meter.power", production, demand), false);
		player.sendMessage(Text.translatable("message.powergrid.meter.balance", (balance >= 0 ? "+" : "") + balance)
				.formatted(balance >= 0 ? Formatting.GREEN : Formatting.RED), false);
		player.sendMessage(Text.translatable("message.powergrid.meter.storage",
				info.stored(), info.storageCapacity(), info.batteries.size()), false);
		player.sendMessage(Text.translatable("message.powergrid.meter.cables", info.cableCount,
				info.cableCount == 0 ? "—" : String.valueOf(info.minCableCapacity)), false);
		return ActionResult.SUCCESS;
	}
}
