package com.matyrobbrt.idd.predicate.entity;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;

public record IsOnFireEntityPredicate() implements EntityPredicate {
    public static final Codec<IsOnFireEntityPredicate> CODEC = Codec.unit(new IsOnFireEntityPredicate());

    @Override
    public boolean test(Entity target, PredicateContext context) {
        return target.isOnFire();
    }

    @Override
    public Codec<? extends EntityPredicate> codec() {
        return CODEC;
    }
}
