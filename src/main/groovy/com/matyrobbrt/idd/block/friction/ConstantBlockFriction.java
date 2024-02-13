package com.matyrobbrt.idd.block.friction;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record ConstantBlockFriction(float value) implements BlockFrictionProvider {
    public static final Codec<ConstantBlockFriction> CODEC = Codec.floatRange(0, 1)
            .xmap(ConstantBlockFriction::new, ConstantBlockFriction::value);

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return value;
    }

    @Override
    public Codec<? extends BlockFrictionProvider> codec() {
        return CODEC;
    }
}
