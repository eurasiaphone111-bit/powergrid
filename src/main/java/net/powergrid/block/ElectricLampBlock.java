package net.powergrid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;
import net.powergrid.blockentity.ElectricLampBlockEntity;
import org.jetbrains.annotations.Nullable;

/** Электролампа: потребляет 4 EU/t и светится, пока сеть её питает. */
public class ElectricLampBlock extends BlockWithEntity implements GridNode {
	public static final MapCodec<ElectricLampBlock> CODEC = createCodec(ElectricLampBlock::new);

	public ElectricLampBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(Properties.LIT, false));
	}

	@Override
	public MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(Properties.LIT);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ElectricLampBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlocks.ELECTRIC_LAMP_BE, ElectricLampBlockEntity::tick);
	}
}
