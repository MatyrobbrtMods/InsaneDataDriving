package com.matyrobbrt.idd.mixin;

import com.matyrobbrt.idd.block.expdrop.ExperienceDropRegistry;
import com.matyrobbrt.idd.block.friction.BlockFrictionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IBlockExtension.class)
public interface BlockMixin {

    @Shadow
    Block self();

    /**
     * @author IDD
     * @reason sigh, thanks mixin
     */
    @Overwrite
    default float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return BlockFrictionRegistry.REGISTRY.getFrom(state.getBlockHolder())
                .map(provider -> provider.getFriction(state, level, pos, entity))
                .orElseGet(() -> self().getFriction());
    }

    /**
     * @author IDD
     * @reason sigh, thanks mixin
     */
    @Overwrite
    default int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        return ExperienceDropRegistry.REGISTRY.getFrom(state.getBlockHolder())
                .map(provider -> provider.getExpDrop(state, level, randomSource, pos, fortuneLevel, silkTouchLevel))
                .orElse(0);
    }
}
