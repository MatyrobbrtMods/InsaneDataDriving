package com.matyrobbrt.idd.predicate.entity;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.matyrobbrt.idd.predicate.item.ItemPredicate;
import com.matyrobbrt.idd.predicate.item.ItemPredicates;
import com.matyrobbrt.idd.util.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public record IsHoldingEntityPredicate(InteractionHand hand, ItemPredicate item) implements EntityPredicate {
    public static final Codec<IsHoldingEntityPredicate> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codecs.INTERACTION_HAND.optionalFieldOf("hand", InteractionHand.MAIN_HAND).forGetter(IsHoldingEntityPredicate::hand),
            ItemPredicates.CODEC.fieldOf("item").forGetter(IsHoldingEntityPredicate::item)
    ).apply(in, IsHoldingEntityPredicate::new));

    @Override
    public boolean test(Entity target, PredicateContext context) {
        if (target instanceof Mob mob) {
            return item.test(mob.getItemInHand(hand), context);
        }
        return false;
    }

    @Override
    public Codec<? extends EntityPredicate> codec() {
        return CODEC;
    }
}
