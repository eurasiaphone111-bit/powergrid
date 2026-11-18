package net.powergrid.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;
import net.powergrid.grid.Grid;

public class BatteryBlockEntity extends BlockEntity {
	public static final long CAPACITY = 100_000; // EU
	public static final int MAX_RATE = 64;        // EU/t

	private long energy;

	public BatteryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.BATTERY_BE, pos, state);
	}

	public long getEnergy() {
		return energy;
	}

	/** Сколько можно отдать за интервал в ticks тиков. */
	public long canExtract(int ticks) {
		return Math.min(energy, (long) MAX_RATE * ticks);
	}

	/** Сколько можно принять за интервал в ticks тиков. */
	public long canInsert(int ticks) {
		return Math.min(CAPACITY - energy, (long) MAX_RATE * ticks);
	}

	public long insert(long amount, int ticks) {
		long accepted = Math.min(amount, canInsert(ticks));
		if (accepted > 0) {
			energy += accepted;
			markDirty();
		}
		return accepted;
	}

	public long extract(long amount, int ticks) {
		long given = Math.min(amount, canExtract(ticks));
		if (given > 0) {
			energy -= given;
			markDirty();
		}
		return given;
	}

	public static void tick(World world, BlockPos pos, BlockState state, BatteryBlockEntity be) {
		Grid.tickSource(world, pos);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putLong("Energy", energy);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		energy = nbt.getLong("Energy");
	}
}
