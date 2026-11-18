package net.powergrid.blockentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;

public class ElectricLampBlockEntity extends BlockEntity {
	public static final int DEMAND = 4; // EU/t

	/** Мировое время последней подачи питания. Если сеть пропала целиком — лампа гаснет сама. */
	private long lastPowered = Long.MIN_VALUE / 2;

	public ElectricLampBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.ELECTRIC_LAMP_BE, pos, state);
	}

	public int getDemand() {
		return DEMAND;
	}

	/** @return true, если лампа была включена и погасла */
	public boolean setPowered(boolean powered) {
		if (world == null) return false;
		if (powered) lastPowered = world.getTime();
		BlockState state = world.getBlockState(pos);
		if (!state.contains(Properties.LIT) || state.get(Properties.LIT) == powered) return false;
		world.setBlockState(pos, state.with(Properties.LIT, powered), Block.NOTIFY_ALL);
		return !powered;
	}

	public static void tick(World world, BlockPos pos, BlockState state, ElectricLampBlockEntity be) {
		// Нет ни одного генератора/аккумулятора в сети — через 2 секунды гаснем.
		if (state.get(Properties.LIT) && world.getTime() - be.lastPowered > 40) {
			be.setPowered(false);
		}
	}
}
