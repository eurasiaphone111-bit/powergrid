package net.powergrid.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;
import net.powergrid.grid.Grid;

public class SolarPanelBlockEntity extends BlockEntity implements EnergySource {
	public static final int OUTPUT_SUN = 8;  // EU/t
	public static final int OUTPUT_RAIN = 2; // EU/t

	public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.SOLAR_PANEL_BE, pos, state);
	}

	@Override
	public int getOutput() {
		if (world == null || !world.isDay() || !world.isSkyVisible(pos.up())) return 0;
		return world.isRaining() ? OUTPUT_RAIN : OUTPUT_SUN;
	}

	public static void tick(World world, BlockPos pos, BlockState state, SolarPanelBlockEntity be) {
		Grid.tickSource(world, pos);
	}
}
