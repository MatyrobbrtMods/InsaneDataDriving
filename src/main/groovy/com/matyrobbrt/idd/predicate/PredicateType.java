package com.matyrobbrt.idd.predicate;

import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface PredicateType<T> {
    String id();

    T concat(T a, T b);

    T or(T a, T b);

    T not(T a);

    Multimap<ResourceLocation, String> getAliases();
}
