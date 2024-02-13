package com.matyrobbrt.idd.predicate.item;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.matyrobbrt.idd.predicate.number.NumberPredicate;
import com.matyrobbrt.idd.predicate.number.NumberPredicates;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

public record CountItemPredicate(NumberPredicate count) implements ItemPredicate {
    public static final Codec<CountItemPredicate> CODEC = NumberPredicates.CODEC
            .fieldOf("count")
            .xmap(CountItemPredicate::new, CountItemPredicate::count)
            .codec();

    @Override
    public boolean test(ItemStack target, PredicateContext context) {
        return count.test(target.getCount());
    }

    @Override
    public Codec<? extends ItemPredicate> codec() {
        return CODEC;
    }
}
