package net.powergrid.blockentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;
import net.powergrid.grid.Grid;

public class CoalGeneratorBlockEntity extends BlockEntity implements EnergySource {
	public static final int OUTPUT = 20;          // EU/t
	public static final int MAX_BURN_TIME = 64000; // тиков (~53 минуты)

	private int burnTime;

	public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.COAL_GENERATOR_BE, pos, state);
	}

	public int getBurnTime() {
		return burnTime;
	}

	/** @return true, если топливо поместилось */
	public boolean addFuel(int ticks) {
		if (burnTime + ticks > MAX_BURN_TIME) return false;
		burnTime += ticks;
		markDirty();
		return true;
	}

	@Override
	public int getOutput() {
		return burnTime > 0 ? OUTPUT : 0;
	}

	public static void tick(World world, BlockPos pos, BlockState state, CoalGeneratorBlockEntity be) {
		if (be.burnTime > 0) {
			be.burnTime--;
			be.markDirty();
		}
		boolean lit = be.burnTime > 0;
		if (state.get(Properties.LIT) != lit) {
			world.setBlockState(pos, state.with(Properties.LIT, lit), Block.NOTIFY_ALL);
		}
		Grid.tickSource(world, pos);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putInt("BurnTime", burnTime);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		burnTime = nbt.getInt("BurnTime");
	}
}
