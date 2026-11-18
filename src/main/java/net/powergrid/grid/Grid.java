package net.powergrid.grid;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.powergrid.block.CableBlock;
import net.powergrid.block.GridNode;
import net.powergrid.blockentity.BatteryBlockEntity;
import net.powergrid.blockentity.ElectricLampBlockEntity;
import net.powergrid.blockentity.EnergySource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Расчёт энергосети.
 * Раз в INTERVAL тиков каждый источник (генератор или аккумулятор) обходит свою сеть.
 * Считает только «главный» источник — тот, у кого минимальные координаты, чтобы сеть
 * не рассчитывалась несколько раз.
 *
 * Логика баланса (как у диспетчера):
 *   генерация ≥ нагрузка  → все потребители питаются, излишек идёт в аккумуляторы;
 *   генерация < нагрузка  → дефицит покрывают аккумуляторы;
 *   не хватает и их       → срабатывает автомат: блэкаут всей сети, генерация заряжает аккумуляторы.
 * Если поток в сети больше пропускной способности самого слабого кабеля — этот кабель сгорает.
 */
public final class Grid {
	public static final int INTERVAL = 20;   // тиков (1 секунда)
	public static final int MAX_NODES = 4096;

	private Grid() {}

	public static final class Info {
		public final List<EnergySource> generators = new ArrayList<>();
		public final List<BatteryBlockEntity> batteries = new ArrayList<>();
		public final List<ElectricLampBlockEntity> consumers = new ArrayList<>();
		public int cableCount = 0;
		public int minCableCapacity = Integer.MAX_VALUE;
		public BlockPos weakestCable = null;
		public long masterKey = Long.MAX_VALUE;

		public int production() {
			int sum = 0;
			for (EnergySource g : generators) sum += g.getOutput();
			return sum;
		}

		public int demand() {
			int sum = 0;
			for (ElectricLampBlockEntity c : consumers) sum += c.getDemand();
			return sum;
		}

		public long stored() {
			long sum = 0;
			for (BatteryBlockEntity b : batteries) sum += b.getEnergy();
			return sum;
		}

		public long storageCapacity() {
			return (long) batteries.size() * BatteryBlockEntity.CAPACITY;
		}
	}

	/** Обход сети в ширину от стартового блока. */
	public static Info scan(World world, BlockPos start) {
		Info info = new Info();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<Long> visited = new HashSet<>();
		queue.add(start);
		visited.add(start.asLong());

		while (!queue.isEmpty() && visited.size() <= MAX_NODES) {
			BlockPos pos = queue.poll();
			BlockState state = world.getBlockState(pos);
			if (!(state.getBlock() instanceof GridNode)) continue;

			if (state.getBlock() instanceof CableBlock cable) {
				info.cableCount++;
				if (cable.getCapacity() < info.minCableCapacity) {
					info.minCableCapacity = cable.getCapacity();
					info.weakestCable = pos;
				}
			}

			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof EnergySource source) {
				info.generators.add(source);
				info.masterKey = Math.min(info.masterKey, pos.asLong());
			} else if (be instanceof BatteryBlockEntity battery) {
				info.batteries.add(battery);
				info.masterKey = Math.min(info.masterKey, pos.asLong());
			} else if (be instanceof ElectricLampBlockEntity lamp) {
				info.consumers.add(lamp);
			}

			for (Direction direction : Direction.values()) {
				BlockPos next = pos.offset(direction);
				if (visited.contains(next.asLong())) continue;
				if (!world.isChunkLoaded(next.getX() >> 4, next.getZ() >> 4)) continue;
				if (!(world.getBlockState(next).getBlock() instanceof GridNode)) continue;
				visited.add(next.asLong());
				queue.add(next.toImmutable());
			}
		}
		return info;
	}

	/** Вызывается из тикера каждого источника. */
	public static void tickSource(World world, BlockPos pos) {
		if (!(world instanceof ServerWorld serverWorld)) return;
		if (world.getTime() % INTERVAL != 0) return;
		Info info = scan(world, pos);
		if (info.masterKey != pos.asLong()) return; // сеть считает другой источник
		solve(serverWorld, info, pos);
	}

	private static void solve(ServerWorld world, Info info, BlockPos master) {
		long produced = (long) info.production() * INTERVAL;
		long demand = (long) info.demand() * INTERVAL;
		long toBatteries = 0;
		boolean powered;

		if (produced >= demand) {
			powered = true;
			toBatteries = charge(info, produced - demand);
		} else {
			long deficit = demand - produced;
			long available = 0;
			for (BatteryBlockEntity b : info.batteries) available += b.canExtract(INTERVAL);
			if (available >= deficit) {
				powered = true;
				for (BatteryBlockEntity b : info.batteries) {
					if (deficit <= 0) break;
					deficit -= b.extract(deficit, INTERVAL);
				}
			} else {
				// Автомат сработал: нагрузка отключена, генерация уходит в аккумуляторы.
				powered = false;
				toBatteries = charge(info, produced);
			}
		}

		// Перегрев кабеля: поток (EU/t) больше пропускной способности самого слабого кабеля.
		long flow = (powered ? info.demand() : 0) + toBatteries / INTERVAL;
		if (info.weakestCable != null && flow > info.minCableCapacity) {
			burnCable(world, info.weakestCable);
		}

		boolean blackout = false;
		for (ElectricLampBlockEntity lamp : info.consumers) {
			blackout |= lamp.setPowered(powered);
		}
		if (blackout) {
			world.playSound(null, master, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1.0f, 0.5f);
		}
	}

	private static long charge(Info info, long amount) {
		long given = 0;
		for (BatteryBlockEntity b : info.batteries) {
			if (amount - given <= 0) break;
			given += b.insert(amount - given, INTERVAL);
		}
		return given;
	}

	private static void burnCable(ServerWorld world, BlockPos pos) {
		world.breakBlock(pos, false);
		world.spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				12, 0.25, 0.25, 0.25, 0.02);
		world.spawnParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				4, 0.2, 0.2, 0.2, 0.0);
		world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
	}
}
