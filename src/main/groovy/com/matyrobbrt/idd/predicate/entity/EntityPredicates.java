package com.matyrobbrt.idd.predicate.entity;

import com.google.common.collect.Multimap;
import com.matyrobbrt.idd.DataCodecRegistry;
import com.matyrobbrt.idd.predicate.PredicateCodec;
import com.matyrobbrt.idd.predicate.PredicateType;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EntityPredicates {
    public static final ResourceKey<Registry<Codec<EntityPredicate>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("idd:entity_predicate"));

    public static final DataCodecRegistry<EntityPredicate> REGISTRY = new DataCodecRegistry<>(
            REGISTRY_KEY, EntityPredicate::codec
    );

    public static final PredicateType<EntityPredicate> TYPE = new PredicateType<>() {
        @Override
        public String id() {
            return "entity";
        }

        @Override
        public EntityPredicate concat(EntityPredicate a, EntityPredicate b) {
            return new AndEntityPredicate(List.of(a, b));
        }

        @Override
        public EntityPredicate or(EntityPredicate a, EntityPredicate b) {
            return new OrEntityPredicate(List.of(a, b));
        }

        @Override
        public EntityPredicate not(EntityPredicate a) {
            return new NotEntityPredicate(a);
        }

        @Override
        public Multimap<ResourceLocation, String> getAliases() {
            return REGISTRY.aliases;
        }
    };

    public static final Codec<EntityPredicate> CODEC = new PredicateCodec<>(
            TYPE, REGISTRY.codec()
    );

    public static final Codec<IsEntityPredicate> IS = REGISTRY.registerDefault("is", IsEntityPredicate.CODEC);
    public static final Codec<AndEntityPredicate> AND = REGISTRY.register("and", AndEntityPredicate.CODEC);
    public static final Codec<NotEntityPredicate> NOT = REGISTRY.register("not", NotEntityPredicate.CODEC);
    public static final Codec<OrEntityPredicate> OR = REGISTRY.register("or", OrEntityPredicate.CODEC);

    public static final Codec<IsOnFireEntityPredicate> IS_ON_FIRE = REGISTRY.register(IsOnFireEntityPredicate.CODEC, "is_on_fire", "isOnFire");
}
