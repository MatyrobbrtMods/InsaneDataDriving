package com.matyrobbrt.idd.block.toolaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record StateMatchesToolModification(StatePropertiesPredicate predicate, ToolModificationProvider transformation) implements ToolModificationProvider {
    public static final Codec<StateMatchesToolModification> CODEC = RecordCodecBuilder.create(in -> in.group(
            StatePropertiesPredicate.CODEC.fieldOf("predicate").forGetter(StateMatchesToolModification::predicate),
            ToolModificationRegistry.REGISTRY.codec().fieldOf("transformation").forGetter(StateMatchesToolModification::transformation)
    ).apply(in, StateMatchesToolModification::new));

    @Override
    public @Nullable BlockState getModifiedState(BlockState state, UseOnContext context, boolean simulate) {
        return predicate.matches(state) ? transformation.getModifiedState(state, context, simulate) : null;
    }

    @Override
    public Codec<? extends ToolModificationProvider> codec() {
        return CODEC;
    }
}
