package com.matyrobbrt.idd.predicate.entity;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;

public interface EntityPredicate {
    boolean test(Entity target, PredicateContext context);

    Codec<? extends EntityPredicate> codec();

}
