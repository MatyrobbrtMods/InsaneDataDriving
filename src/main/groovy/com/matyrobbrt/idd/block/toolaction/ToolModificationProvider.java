package com.matyrobbrt.idd.block.toolaction;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ToolModificationProvider {
    @Nullable BlockState getModifiedState(BlockState state, UseOnContext context, boolean simulate);

    Codec<? extends ToolModificationProvider> codec();
}
