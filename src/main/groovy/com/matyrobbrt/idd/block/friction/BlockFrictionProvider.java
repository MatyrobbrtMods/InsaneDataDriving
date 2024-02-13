package com.matyrobbrt.idd.block.friction;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface BlockFrictionProvider {
    float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity);

    Codec<? extends BlockFrictionProvider> codec();
}
