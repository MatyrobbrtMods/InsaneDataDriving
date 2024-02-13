package com.matyrobbrt.idd.util.codec;

import com.matyrobbrt.idd.predicate.PredicateType;
import com.matyrobbrt.idd.predicate.script.FieldType;
import com.matyrobbrt.idd.util.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecDecomposer {
    private final List<TypeGetter> typeGetters = new ArrayList<>();
    private final List<RawTypeGetter> rawGetters = new ArrayList<>();

    public CodecDecomposer registerCodec(Codec<?> codec, FieldType type) {
        rawGetters.add(c1 -> c1 == codec ? type : null);
        return this;
    }

    public CodecDecomposer registerCodec(RawTypeGetter getter) {
        rawGetters.add(getter);
        return this;
    }

    public CodecDecomposer register(TypeGetter getter) {
        typeGetters.add(getter);
        return this;
    }

    public CodecDecomposer registerPrimitives() {
        return registerCodec(Codec.STRING, FieldType.Primitive.STRING)
                .registerCodec(Codec.INT, FieldType.Primitive.INT)

                // TODO - we need to support comapFlatMap
                .registerCodec(ResourceLocation.CODEC, FieldType.Primitive.STRING);
    }

    public CodecDecomposer registerDefaults() {
        return registerCodec(codec -> codec instanceof ListCodec<?> listCodec ? new FieldType.List(decomposeUnsafe(RCBDecomposing.getElementCodec(listCodec))) : null)
                .registerCodec(codec -> {
                    if (codec instanceof UnboundedMapCodec<?,?> unbounded) {
                        return new FieldType.MapType(decomposeUnsafe(unbounded.keyCodec()), decomposeUnsafe(unbounded.elementCodec()));
                    }
                    return null;
                })
                .registerCodec(codec -> codec instanceof Codecs.ListOrSingleCodec<?> lst ? new FieldType.List(decomposeUnsafe(lst.simpleCodec())) : null)
                .registerCodec(codec -> codec instanceof PredicateType<?> pred ? new FieldType.Predicate(pred) : null);
    }

    public FieldType decomposeUnsafe(Codec<?> codec) {
        try {
            return decompose(codec);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public FieldType decompose(Codec<?> codec) throws Throwable {
        codec = CodecDecompositionUtil.CODEC_EXPANDER.expand(codec);
        for (final var raw : rawGetters) {
            final var type = raw.getType(codec);
            if (type != null) return type;
        }

        try {
            final var types = new HashMap<String, FieldType>();
            final var underlying = RCBDecomposing.getUnderlying(codec);
            RCBDecomposing.decomposeRecursively(underlying)
                    .map(RCBDecomposing::getUnderlyingDecoder)
                    .map(c -> {
                        try {
                            return CodecDecompositionUtil.MAP_DECODER_TO_FIELD.expand(
                                    CodecDecompositionUtil.MAP_DECODER_EXPANDER.expand(c)
                            );
                        } catch (Throwable ignored) {
                            throw new RuntimeException(ignored);
                        }
                    })
                    .forEach(dec -> types.put(dec.name(), dec.optional() ? new FieldType.Optional(getType(dec.component())) : getType(dec.component())));

            return new FieldType.Object(types);
        } catch (Exception ignored) {

        }

        if (codec instanceof MapCodec.MapCodecCodec<?> mapCodecCodec) {
            if (CodecDecompositionUtil.isUnit(RCBDecomposing.decompose(mapCodecCodec.codec()).decoder())) {
                return FieldType.UNIT;
            }

            final var field = CodecDecompositionUtil.MAP_DECODER_TO_FIELD.expand(mapCodecCodec.codec());
            if (field != null) {
                final var type = getType(field.component());
                return new FieldType.Object(Map.of(
                        field.name(), field.optional() ? new FieldType.Optional(type) : type
                ));
            }
        }

        final var extracted = CodecDecompositionUtil.CODEC_EXTRACTOR.expand(codec);
        if (extracted != null && extracted.decoder() != codec) {
            return getType(extracted.decoder());
        }

        return FieldType.JAVA_OBJECT;
    }

    private FieldType getType(Decoder<?> decoder) {
        if (decoder instanceof Codec<?> cd) {
            return decomposeUnsafe(cd);
        }

        for (final var get : typeGetters) {
            final var type = get.getType(decoder);
            if (type != null) return type;
        }

        return FieldType.JAVA_OBJECT;
    }

    public interface TypeGetter {
        @Nullable FieldType getType(Decoder<?> decoder);
    }

    public interface RawTypeGetter {
        @Nullable FieldType getType(Codec<?> codec);
    }
}
