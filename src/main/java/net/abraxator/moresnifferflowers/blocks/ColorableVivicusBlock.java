package net.abraxator.moresnifferflowers.blocks;

import com.google.common.collect.Maps;
import net.abraxator.moresnifferflowers.components.Colorable;
import net.abraxator.moresnifferflowers.components.Dye;
import net.abraxator.moresnifferflowers.init.ModStateProperties;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Map;

public interface ColorableVivicusBlock extends Colorable {
    default EnumProperty<DyeColor> getColorProperty() {
        return ModStateProperties.COLOR;
    }

    default void addDye(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if(!blockState.hasProperty(getColorProperty())) {
            return;
        }
        
        var stack = player.getMainHandItem();
        Dye dye = Dye.getDyeFromStack(stack);
        RandomSource randomSource = level.random;

        if(blockState.getValue(getColorProperty()).equals(dye.color()) || dye.isEmpty()) {
            return;
        }

        level.setBlockAndUpdate(blockPos, blockState.setValue(getColorProperty(), dye.color()));
        ItemStack itemStack = Dye.stackFromDye(new Dye(dye.color(), dye.amount() - 1));
        Dye.setDyeToStack(stack, itemStack, itemStack.getCount());
        
        if(!level.isClientSide()) {
            particles(randomSource, ((ServerLevel) level), dye, blockPos);
        }
    }

    @Override
    default Map<DyeColor, Integer> colorValues() {
        return Util.make(Maps.newLinkedHashMap(), dyeColorHexFormatMap -> {
            dyeColorHexFormatMap.put(DyeColor.WHITE, 0xFFFFFFFF);
            dyeColorHexFormatMap.put(DyeColor.LIGHT_GRAY, 0xFF9d979b);
            dyeColorHexFormatMap.put(DyeColor.GRAY, 0xFF474f52);
            dyeColorHexFormatMap.put(DyeColor.BLACK, 0xFF1d1d21);
            dyeColorHexFormatMap.put(DyeColor.BROWN, 0xFF835432);
            dyeColorHexFormatMap.put(DyeColor.RED, 0xFFb5432e);
            dyeColorHexFormatMap.put(DyeColor.ORANGE, 0xFFf89635);
            dyeColorHexFormatMap.put(DyeColor.YELLOW, 0xFFffee53);
            dyeColorHexFormatMap.put(DyeColor.LIME, 0xFF80c71f);
            dyeColorHexFormatMap.put(DyeColor.GREEN, 0xFF5e7c16);
            dyeColorHexFormatMap.put(DyeColor.CYAN, 0xFF00AACC);
            dyeColorHexFormatMap.put(DyeColor.LIGHT_BLUE, 0xFF70d9e4);
            dyeColorHexFormatMap.put(DyeColor.BLUE, 0xFF4753ac);
            dyeColorHexFormatMap.put(DyeColor.PURPLE, 0xFFb15fc2);
            dyeColorHexFormatMap.put(DyeColor.MAGENTA, 0xFFd276b9);
            dyeColorHexFormatMap.put(DyeColor.PINK, 0xFFf8b0c4);
        });
    }
}
