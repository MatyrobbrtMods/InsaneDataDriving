package com.matyrobbrt.idd.predicate.entity;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.matyrobbrt.idd.util.Codecs;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record AndEntityPredicate(List<EntityPredicate> predicates) implements EntityPredicate {
    public static final Codec<AndEntityPredicate> CODEC = Codecs.listOf(EntityPredicates.CODEC)
            .fieldOf("predicates")
            .xmap(AndEntityPredicate::new, AndEntityPredicate::predicates)
            .codec();

    @Override
    public boolean test(Entity target, PredicateContext context) {
        return predicates.stream().allMatch(pred -> pred.test(target, context));
    }

    @Override
    public Codec<? extends EntityPredicate> codec() {
        return CODEC;
    }
}
