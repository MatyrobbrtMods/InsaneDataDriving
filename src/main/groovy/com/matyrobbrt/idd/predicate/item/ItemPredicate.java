package com.matyrobbrt.idd.predicate.item;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

public interface ItemPredicate {
    boolean test(ItemStack target, PredicateContext context);

    Codec<? extends ItemPredicate> codec();
}
