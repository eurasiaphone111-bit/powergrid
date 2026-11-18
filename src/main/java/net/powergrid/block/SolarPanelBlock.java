package net.powergrid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;
import net.powergrid.blockentity.SolarPanelBlockEntity;
import org.jetbrains.annotations.Nullable;

/** Солнечная панель: 8 EU/t днём под открытым небом, 2 EU/t в дождь, 0 ночью. */
public class SolarPanelBlock extends BlockWithEntity implements GridNode {
	public static final MapCodec<SolarPanelBlock> CODEC = createCodec(SolarPanelBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 6, 16);

	public SolarPanelBlock(Settings settings) {
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

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SolarPanelBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlocks.SOLAR_PANEL_BE, SolarPanelBlockEntity::tick);
	}
}
