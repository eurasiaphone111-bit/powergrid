package net.powergrid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.powergrid.ModBlocks;
import net.powergrid.blockentity.CoalGeneratorBlockEntity;
import org.jetbrains.annotations.Nullable;

/** Угольный генератор: ПКМ углём/древесным углём/угольным блоком — заправить. Пока горит, даёт 20 EU/t. */
public class CoalGeneratorBlock extends BlockWithEntity implements GridNode {
	public static final MapCodec<CoalGeneratorBlock> CODEC = createCodec(CoalGeneratorBlock::new);

	public CoalGeneratorBlock(Settings settings) {
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
		return new CoalGeneratorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : validateTicker(type, ModBlocks.COAL_GENERATOR_BE, CoalGeneratorBlockEntity::tick);
	}

	@Override
	public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
											 PlayerEntity player, Hand hand, BlockHitResult hit) {
		int fuel = 0;
		if (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL)) fuel = 1600;
		else if (stack.isOf(Items.COAL_BLOCK)) fuel = 16000;
		if (fuel == 0) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if (!world.isClient && world.getBlockEntity(pos) instanceof CoalGeneratorBlockEntity generator) {
			if (generator.addFuel(fuel)) {
				if (!player.getAbilities().creativeMode) stack.decrement(1);
				world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.6f, 1.2f);
			}
			player.sendMessage(Text.translatable("message.powergrid.fuel", generator.getBurnTime() / 20), true);
		}
		return ItemActionResult.success(world.isClient);
	}
}
