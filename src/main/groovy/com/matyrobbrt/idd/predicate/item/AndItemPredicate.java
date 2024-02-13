package com.matyrobbrt.idd.predicate.item;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.matyrobbrt.idd.util.Codecs;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AndItemPredicate(List<ItemPredicate> predicates) implements ItemPredicate {
    public static final Codec<AndItemPredicate> CODEC = Codecs.listOf(ItemPredicates.CODEC)
            .fieldOf("predicates")
            .xmap(AndItemPredicate::new, AndItemPredicate::predicates)
            .codec();

    @Override
    public boolean test(ItemStack target, PredicateContext context) {
        return predicates.stream().allMatch(pred -> pred.test(target, context));
    }

    @Override
    public Codec<? extends ItemPredicate> codec() {
        return CODEC;
    }
}
