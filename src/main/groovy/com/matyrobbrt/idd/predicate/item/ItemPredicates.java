package com.matyrobbrt.idd.predicate.item;

import com.google.common.collect.Multimap;
import com.matyrobbrt.idd.DataCodecRegistry;
import com.matyrobbrt.idd.predicate.PredicateCodec;
import com.matyrobbrt.idd.predicate.PredicateType;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ItemPredicates {
    public static final ResourceKey<Registry<Codec<ItemPredicate>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("idd:item_predicate"));

    public static final DataCodecRegistry<ItemPredicate> REGISTRY = new DataCodecRegistry<>(
            REGISTRY_KEY, ItemPredicate::codec
    );

    public static final PredicateType<ItemPredicate> TYPE = new PredicateType<>() {
        @Override
        public String id() {
            return "item";
        }

        @Override
        public ItemPredicate concat(ItemPredicate a, ItemPredicate b) {
            return new AndItemPredicate(List.of(a, b));
        }

        @Override
        public ItemPredicate or(ItemPredicate a, ItemPredicate b) {
            return new OrItemPredicate(List.of(a, b));
        }

        @Override
        public ItemPredicate not(ItemPredicate a) {
            return null;
        }

        @Override
        public Multimap<ResourceLocation, String> getAliases() {
            return REGISTRY.aliases;
        }

        @Override
        public Registry<Codec<ItemPredicate>> registry() {
            return REGISTRY.registry();
        }
    };

    public static final Codec<ItemPredicate> CODEC = new PredicateCodec<>(
            TYPE, REGISTRY.codec()
    );

    public static final Codec<IsItemPredicate> IS = REGISTRY.registerDefault("is", IsItemPredicate.CODEC);
    public static final Codec<CountItemPredicate> COUNT = REGISTRY.register("count", CountItemPredicate.CODEC);

    public static final Codec<AndItemPredicate> AND = REGISTRY.register("and", AndItemPredicate.CODEC);
    public static final Codec<OrItemPredicate> OR = REGISTRY.register("or", OrItemPredicate.CODEC);
}
