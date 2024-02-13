package com.matyrobbrt.idd.predicate.entity;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.matyrobbrt.idd.util.Codecs;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record OrEntityPredicate(List<EntityPredicate> predicates) implements EntityPredicate {
    public static final Codec<OrEntityPredicate> CODEC = Codecs.listOf(EntityPredicates.CODEC)
            .fieldOf("predicates")
            .xmap(OrEntityPredicate::new, OrEntityPredicate::predicates)
            .codec();

    @Override
    public boolean test(Entity target, PredicateContext context) {
        return predicates.stream().anyMatch(p -> p.test(target, context));
    }

    @Override
    public Codec<? extends EntityPredicate> codec() {
        return CODEC;
    }
}
