package com.matyrobbrt.idd.predicate.item;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record IsItemPredicate(Item item) implements ItemPredicate {
    public static final Codec<IsItemPredicate> CODEC = BuiltInRegistries.ITEM.byNameCodec()
            .fieldOf("item")
            .xmap(IsItemPredicate::new, IsItemPredicate::item)
            .codec();

    @Override
    public boolean test(ItemStack target, PredicateContext context) {
        return target.is(item);
    }

    @Override
    public Codec<? extends ItemPredicate> codec() {
        return CODEC;
    }
}
