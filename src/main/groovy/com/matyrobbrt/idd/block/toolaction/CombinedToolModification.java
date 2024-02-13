package com.matyrobbrt.idd.block.toolaction;

import com.matyrobbrt.idd.util.Codecs;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CombinedToolModification(List<ToolModificationProvider> providers) implements ToolModificationProvider {
    public static final Codec<CombinedToolModification> CODEC = Codecs.listOf(ToolModificationRegistry.REGISTRY.codec())
            .fieldOf("modifications")
            .xmap(CombinedToolModification::new, CombinedToolModification::providers)
            .codec();

    @Override
    public @Nullable BlockState getModifiedState(BlockState state, UseOnContext context, boolean simulate) {
        for (ToolModificationProvider provider : providers) {
            BlockState modified = provider.getModifiedState(state, context, simulate);
            if (modified != null) {
                return modified;
            }
        }
        return null;
    }

    @Override
    public Codec<? extends ToolModificationProvider> codec() {
        return CODEC;
    }
}
