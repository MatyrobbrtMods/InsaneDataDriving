package com.matyrobbrt.idd.block.toolaction;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record ConstantToolModification(BlockState state) implements ToolModificationProvider {
    // TODO - this uses uppercase
    public static final Codec<ConstantToolModification> CODEC = BlockState.CODEC
            .xmap(ConstantToolModification::new, ConstantToolModification::state);

    @Override
    public @Nullable BlockState getModifiedState(BlockState state, UseOnContext context, boolean simulate) {
        return state;
    }

    @Override
    public Codec<? extends ToolModificationProvider> codec() {
        return CODEC;
    }
}
