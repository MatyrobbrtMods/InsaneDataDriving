package com.matyrobbrt.idd.predicate.number;

import com.google.common.collect.Multimap;
import com.matyrobbrt.idd.DataCodecRegistry;
import com.matyrobbrt.idd.predicate.PredicateCodec;
import com.matyrobbrt.idd.predicate.PredicateType;
import com.matyrobbrt.idd.predicate.item.AndItemPredicate;
import com.matyrobbrt.idd.predicate.item.ItemPredicate;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class NumberPredicates {
    public static final ResourceKey<Registry<Codec<NumberPredicate>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("idd:number_predicate"));

    public static final DataCodecRegistry<NumberPredicate> REGISTRY = new DataCodecRegistry<>(
            REGISTRY_KEY, NumberPredicate::codec
    );

    public static final PredicateType<NumberPredicate> TYPE = new PredicateType<>() {
        @Override
        public String id() {
            return "number";
        }

        @Override
        public NumberPredicate concat(NumberPredicate a, NumberPredicate b) {
            return null;
        }

        @Override
        public NumberPredicate or(NumberPredicate a, NumberPredicate b) {
            return null;
        }

        @Override
        public NumberPredicate not(NumberPredicate a) {
            return null;
        }

        @Override
        public Multimap<ResourceLocation, String> getAliases() {
            return REGISTRY.aliases;
        }

        @Override
        public Registry<Codec<NumberPredicate>> registry() {
            return REGISTRY.registry();
        }
    };

    public static final Codec<NumberPredicate> CODEC = new PredicateCodec<>(
            TYPE, REGISTRY.codec()
    );

    public static final Codec<EqualNumberPredicate> EQUAL = REGISTRY.registerDefault("equal", EqualNumberPredicate.CODEC);
    public static final Codec<GreaterNumberPredicate> GREATER = REGISTRY.register("greater", GreaterNumberPredicate.CODEC);
}
