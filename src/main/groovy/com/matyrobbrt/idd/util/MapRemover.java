package com.matyrobbrt.idd.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record MapRemover<R, K, V>(List<K> keys) implements DataMapValueRemover<R, Map<K, V>> {

    public static <R, K, V> Codec<MapRemover<R, K, V>> codec(Codec<K> keyCodec) {
        return Codecs.listOf(keyCodec)
                .xmap(MapRemover::new, MapRemover::keys);
    }

    @Override
    public Optional<Map<K, V>> remove(Map<K, V> value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, R object) {
        final var newMap = ImmutableMap.<K, V>builder();
        value.forEach((key, v) -> {
            if (!keys.contains(key)) {
                newMap.put(key, v);
            }
        });
        return Optional.of(newMap.build());
    }
}
