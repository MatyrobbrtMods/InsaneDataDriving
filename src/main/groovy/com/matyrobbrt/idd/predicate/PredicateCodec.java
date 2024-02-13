package com.matyrobbrt.idd.predicate;

import com.matyrobbrt.idd.util.Reflections;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.JavaOps;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

public record PredicateCodec<P>(PredicateType<P> type, Codec<P> delegate) implements Codec<P> {

    @Override
    public <T> DataResult<Pair<P, T>> decode(DynamicOps<T> ops, T input) {
        if (ops instanceof PredicateCodec.PredicateOps<T> pre && pre.scriptDecoding) {
            final var asStr = ops.getStringValue(input);
            if (asStr.result().isPresent()) {
                // TODO - more efficient
                return PredicateTypes.getFactory(type).evaluateOrError(asStr.result().get(), new PredicateOps<>(RegistryOps.create(
                        JavaOps.INSTANCE, pre.lookup
                ), true)).map(p -> Pair.of(p, ops.empty()));
            }
        }
        if (input instanceof PredicateReference pref) {
            return DataResult.success(Pair.of(
                    (P) pref.getReference(),
                    ops.empty()
            ));
        }
        return delegate.decode(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(P input, DynamicOps<T> ops, T prefix) {
        return delegate.encode(input, ops, prefix);
    }

    public static class PredicateOps<T> extends RegistryOps<T> {
        public final RegistryOps.RegistryInfoLookup lookup;
        public final boolean scriptDecoding;
        public PredicateOps(RegistryOps<T> ops, boolean scriptDecoding) {
            super(ops);
            this.scriptDecoding = scriptDecoding;
            this.lookup = Reflections.getField("lookupProvider", ops);
        }
    }
}
