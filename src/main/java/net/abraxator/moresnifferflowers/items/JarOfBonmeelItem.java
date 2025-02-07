package net.abraxator.moresnifferflowers.items;

import net.abraxator.moresnifferflowers.blocks.GiantCropBlock;
import net.abraxator.moresnifferflowers.blockentities.GiantCropBlockEntity;
import net.abraxator.moresnifferflowers.init.ModAdvancementCritters;
import net.abraxator.moresnifferflowers.init.ModBlocks;
import net.abraxator.moresnifferflowers.init.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.stream.StreamSupport;

public class JarOfBonmeelItem extends Item {
    public final Map<Block, Pair<Block, Pair<IntegerProperty, Integer>>> MAP = Map.of(
            Blocks.CARROTS, new Pair<>(ModBlocks.GIANT_CARROT.get(), new Pair<>(CropBlock.AGE, CropBlock.MAX_AGE)),
            Blocks.POTATOES, new Pair<>(ModBlocks.GIANT_POTATO.get(), new Pair<>(CropBlock.AGE, CropBlock.MAX_AGE)),
            Blocks.NETHER_WART, new Pair<>(ModBlocks.GIANT_NETHERWART.get(), new Pair<>(NetherWartBlock.AGE, NetherWartBlock.MAX_AGE)),
            Blocks.BEETROOTS, new Pair<>(ModBlocks.GIANT_BEETROOT.get(), new Pair<>(BeetrootBlock.AGE, BeetrootBlock.MAX_AGE)),
            Blocks.WHEAT, new Pair<>(ModBlocks.GIANT_WHEAT.get(), new Pair<>(CropBlock.AGE, CropBlock.MAX_AGE))
    );

    public JarOfBonmeelItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Player player = pContext.getPlayer();
        Level level = pContext.getLevel();
        BlockPos clickedPos = pContext.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if(!clickedState.is(ModTags.ModBlockTags.BONMEELABLE)) return InteractionResult.PASS;
        Block crop = clickedState.getBlock();
        Block giantVersion = MAP.get(crop).getA();
        Iterable<BlockPos> blockPosList = BlockPos.betweenClosed(
                clickedPos.getX() - 1,
                clickedPos.getY() - 0,
                clickedPos.getZ() - 1,
                clickedPos.getX() + 1,
                clickedPos.getY() + 2,
                clickedPos.getZ() + 1
        );
        boolean flag = StreamSupport.stream(blockPosList.spliterator(), false).allMatch(pos -> {
            BlockState blockState = level.getBlockState(pos);
            int cropY = clickedPos.getY();
            var PROPERTY = MAP.get(crop).getB().getA();
            int MAX_AGE = MAP.get(crop).getB().getB();

            if(pos.getY() == cropY) {
                return blockState.is(clickedState.getBlock()) && blockState.is(ModTags.ModBlockTags.BONMEELABLE) && blockState.getValue(PROPERTY) == MAX_AGE;
            } else {
                return blockState.is(Blocks.AIR);
            }
        });

        if (pContext.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (flag && !level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            return placeLogic(blockPosList, level, giantVersion, clickedPos, serverPlayer);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult placeLogic(Iterable<BlockPos> blockPosList, Level level, Block giantVersion, BlockPos clickedPos, ServerPlayer player) {
        blockPosList.forEach(pos -> {
            pos = pos.immutable();
            level.destroyBlock(pos, false);
            level.setBlockAndUpdate(pos, giantVersion.defaultBlockState().setValue(GiantCropBlock.MODEL_POSITION, evaulateModelPos(pos, clickedPos)));
            if(level.getBlockEntity(pos) instanceof GiantCropBlockEntity entity) {
                entity.pos1 = clickedPos.mutable().move(1, 2, 1);
                entity.pos2 = clickedPos.mutable().move(-1, 0, -1);
            }
        });

        if(!player.getAbilities().instabuild) {
            player.getMainHandItem().shrink(1);
        }
        
        ModAdvancementCritters.USED_BONMEEL.trigger(player);
        level.playLocalSound(clickedPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private GiantCropBlock.ModelPos evaulateModelPos(BlockPos pos, BlockPos posToCompare) {
        var value = GiantCropBlock.ModelPos.NONE;

        posToCompare = posToCompare.above();
        pos = pos.above();

        if(pos.equals(posToCompare.north().east())) {
            value = GiantCropBlock.ModelPos.NEU;
        }
        if(pos.equals(posToCompare.north().west())) {
            value = GiantCropBlock.ModelPos.NWU;
        }
        if(pos.equals(posToCompare.south().east())) {
            value = GiantCropBlock.ModelPos.SEU;
        }
        if(pos.equals(posToCompare.south().west())) {
            value = GiantCropBlock.ModelPos.SWU;
        }

        posToCompare.below(2);
        pos = pos.below(2);

        if(pos.equals(posToCompare.north().east())) {
            value = GiantCropBlock.ModelPos.NED;
        }
        if(pos.equals(posToCompare.north().west())) {
            value = GiantCropBlock.ModelPos.NWD;
        }
        if(pos.equals(posToCompare.south().east())) {
            value = GiantCropBlock.ModelPos.SED;
        }
        if(pos.equals(posToCompare.south().west())) {
            value = GiantCropBlock.ModelPos.SWD;
        }

        return value;
    }



    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatableWithFallback("tooltip.jar_of_bonmeel.usage", "Can be applied to a 3x3 grid of the following crops: carrot, potato, wheat, beetroot and nether wart").withStyle(ChatFormatting.GOLD));
    }
}
