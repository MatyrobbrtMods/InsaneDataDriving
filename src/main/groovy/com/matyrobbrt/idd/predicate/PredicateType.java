package com.matyrobbrt.idd.predicate;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface PredicateType<T> {
    String id();

    T concat(T a, T b);

    T or(T a, T b);

    T not(T a);

    Registry<Codec<T>> registry();

    Multimap<ResourceLocation, String> getAliases();
}
