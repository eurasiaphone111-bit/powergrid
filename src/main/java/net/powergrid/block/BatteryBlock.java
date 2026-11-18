package net.powergrid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;
import net.powergrid.blockentity.BatteryBlockEntity;
import org.jetbrains.annotations.Nullable;

/** Аккумулятор: 100 000 EU, заряд/разряд до 64 EU/t. */
public class BatteryBlock extends BlockWithEntity implements GridNode {
	public static final MapCodec<BatteryBlock> CODEC = createCodec(BatteryBlock::new);

	public BatteryBlock(Settings settings) {
		super(settings);
	}

	@Override
	public MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BatteryBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlocks.BATTERY_BE, BatteryBlockEntity::tick);
	}
}
