package com.matyrobbrt.idd.predicate.entity;

import com.matyrobbrt.idd.predicate.PredicateContext;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record IsEntityPredicate(EntityType<?> type) implements EntityPredicate {
    public static final Codec<IsEntityPredicate> CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec()
            .fieldOf("entity")
            .xmap(IsEntityPredicate::new, IsEntityPredicate::type)
            .codec();

    @Override
    public boolean test(Entity target, PredicateContext context) {
        return target.getType() == type;
    }

    @Override
    public Codec<? extends EntityPredicate> codec() {
        return CODEC;
    }
}
