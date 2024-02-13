package com.matyrobbrt.idd.block.expdrop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public record RangeExperienceDrop(IntProvider range, boolean acceptsSilkTouch) implements ExperienceDropProvider {
    public static final Codec<RangeExperienceDrop> CODEC = RecordCodecBuilder.create(in -> in.group(
            IntProvider.codec(0, Integer.MAX_VALUE).fieldOf("range").forGetter(RangeExperienceDrop::range),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "silk_touch", false).forGetter(RangeExperienceDrop::acceptsSilkTouch)
    ).apply(in, RangeExperienceDrop::new));

    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        return silkTouchLevel == 0 || acceptsSilkTouch ? range.sample(randomSource) : 0;
    }

    @Override
    public Codec<? extends ExperienceDropProvider> codec() {
        return CODEC;
    }
}
