package com.matyrobbrt.idd.predicate.entity;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;

public record NotEntityPredicate(EntityPredicate predicate) implements EntityPredicate {
    public static final Codec<NotEntityPredicate> CODEC = EntityPredicates.CODEC
            .fieldOf("predicate")
            .xmap(NotEntityPredicate::new, NotEntityPredicate::predicate)
            .codec();

    @Override
    public boolean test(Entity target, PredicateContext context) {
        return !predicate.test(target, context);
    }

    @Override
    public Codec<? extends EntityPredicate> codec() {
        return CODEC;
    }
}
