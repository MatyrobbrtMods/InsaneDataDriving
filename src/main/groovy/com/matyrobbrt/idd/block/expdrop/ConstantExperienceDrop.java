package com.matyrobbrt.idd.block.expdrop;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public record ConstantExperienceDrop(int value) implements ExperienceDropProvider {
    public static final Codec<ConstantExperienceDrop> CODEC = Codec.intRange(0, Integer.MAX_VALUE)
            .xmap(ConstantExperienceDrop::new, ConstantExperienceDrop::value);
    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        return value;
    }

    @Override
    public Codec<? extends ExperienceDropProvider> codec() {
        return CODEC;
    }
}
