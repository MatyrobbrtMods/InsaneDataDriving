package com.matyrobbrt.idd;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import net.neoforged.neoforge.registries.datamaps.IWithData;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class DataCodecRegistry<T> {
    public final ResourceKey<Registry<Codec<T>>> key;
    private final Registry<Codec<T>> registry;
    private Codec<T> codec;
    private final DeferredRegister<Codec<T>> deferredRegister;
    public final Multimap<ResourceLocation, String> aliases = Multimaps.newMultimap(new HashMap<>(), ArrayList::new);

    public DataCodecRegistry(ResourceKey<Registry<Codec<T>>> key, Function<T, Codec<? extends T>> codecGetter) {
        this.key = key;
        this.deferredRegister = DeferredRegister.create(key, key.location().getNamespace());
        this.registry = deferredRegister.makeRegistry(builder -> {});
        this.codec = registry.byNameCodec().dispatch(t -> (Codec<T>) codecGetter.apply(t), Function.identity());
    }

    public Registry<Codec<T>> registry() {
        return registry;
    }

    public Codec<T> codec() {
        return ExtraCodecs.lazyInitializedCodec(() -> this.codec);
    }

    public <Z extends T> Codec<Z> register(String name, Codec<Z> codec) {
        deferredRegister.register(name, () -> (Codec) codec);
        return codec;
    }

    public <Z extends T> Codec<Z> register(Codec<Z> codec, String name, String... aliases) {
        final var id = new ResourceLocation(deferredRegister.getNamespace(), name);
        for (String alias : aliases) {
            this.aliases.put(id, alias);
        }
        return register(name, codec);
    }

    // TODO - fix this, recursion
    public <Z extends T> Codec<Z> register(String name, Supplier<Codec<Z>> codec) {
        final var thing = deferredRegister.register(name, () -> (Codec<T>) codec.get());
        return ExtraCodecs.lazyInitializedCodec(() -> (Codec) thing.get());
    }

    public <Z extends T> Codec<Z> registerDefault(String name, Codec<Z> codec) {
        this.codec = ExtraCodecs.withAlternative(this.codec, codec);
        return register(name, codec);
    }

    private DataMapType<?, T> map;
    public <R> DataMapType<R, T> dataMap(String name, ResourceKey<Registry<R>> registry) {
        final Codec<T> codec = ExtraCodecs.lazyInitializedCodec(() -> this.codec);
        final var type = DataMapType.builder(new ResourceLocation(key.location().getNamespace(), name), registry, codec)
                        .synced(codec, true).build();
        map = type;
        return type;
    }

    public void register(IEventBus bus) {
        deferredRegister.register(bus);
        bus.addListener(RegisterDataMapTypesEvent.class, event -> {
            if (map != null) event.register(map);
        });
    }

    public Optional<T> getFrom(IWithData<?> holder) {
        return Optional.ofNullable((T) holder.getData((DataMapType) map));
    }
}
