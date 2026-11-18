package net.powergrid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кабель. Соединяется с любыми блоками энергосети.
 * capacity — пропускная способность в EU/t: если поток в сети больше, самый слабый кабель сгорает.
 */
public class CableBlock extends Block implements GridNode {
	public static final MapCodec<CableBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("capacity").forGetter(CableBlock::getCapacity),
			createSettingsCodec()
	).apply(instance, CableBlock::new));

	private static final VoxelShape CORE = Block.createCuboidShape(6, 6, 6, 10, 10, 10);
	private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);
	static {
		ARMS.put(Direction.NORTH, Block.createCuboidShape(6, 6, 0, 10, 10, 6));
		ARMS.put(Direction.SOUTH, Block.createCuboidShape(6, 6, 10, 10, 10, 16));
		ARMS.put(Direction.WEST, Block.createCuboidShape(0, 6, 6, 6, 10, 10));
		ARMS.put(Direction.EAST, Block.createCuboidShape(10, 6, 6, 16, 10, 10));
		ARMS.put(Direction.DOWN, Block.createCuboidShape(6, 0, 6, 10, 6, 10));
		ARMS.put(Direction.UP, Block.createCuboidShape(6, 10, 6, 10, 16, 10));
	}
	private final Map<BlockState, VoxelShape> shapeCache = new ConcurrentHashMap<>();

	private final int capacity;

	public CableBlock(int capacity, Settings settings) {
		super(settings);
		this.capacity = capacity;
		BlockState state = this.stateManager.getDefaultState();
		for (BooleanProperty property : ConnectingBlock.FACING_PROPERTIES.values()) {
			state = state.with(property, false);
		}
		setDefaultState(state);
	}

	public int getCapacity() {
		return capacity;
	}

	@Override
	public MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ConnectingBlock.NORTH, ConnectingBlock.EAST, ConnectingBlock.SOUTH,
				ConnectingBlock.WEST, ConnectingBlock.UP, ConnectingBlock.DOWN);
	}

	private static boolean connects(BlockState neighbor) {
		return neighbor.getBlock() instanceof GridNode;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockView world = ctx.getWorld();
		BlockPos pos = ctx.getBlockPos();
		BlockState state = getDefaultState();
		for (Direction direction : Direction.values()) {
			state = state.with(ConnectingBlock.FACING_PROPERTIES.get(direction),
					connects(world.getBlockState(pos.offset(direction))));
		}
		return state;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
												   WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		return state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), connects(neighborState));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return shapeCache.computeIfAbsent(state, s -> {
			VoxelShape shape = CORE;
			for (Direction direction : Direction.values()) {
				if (s.get(ConnectingBlock.FACING_PROPERTIES.get(direction))) {
					shape = VoxelShapes.union(shape, ARMS.get(direction));
				}
			}
			return shape;
		});
	}
}
