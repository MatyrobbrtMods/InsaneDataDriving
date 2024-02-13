package com.matyrobbrt.idd.util.codec;

import com.matyrobbrt.idd.util.Reflections;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.FieldDecoder;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import net.minecraft.util.ExtraCodecs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CodecDecompositionUtil {
    public static class Expander<A, B> {
        private final List<Expansion<A, B>> expansions = new ArrayList<>();
        private final Function<A, B> fallback;

        public Expander(Function<A, B> fallback) {
            this.fallback = fallback;
        }

        public static <A> Expander<A, A> identity() {
            return new Expander<>(Function.identity());
        }

        public Expander<A, B> expand(Expansion<A, B> def) {
            this.expansions.add(def);
            return this;
        }

        public Expander<A, B> expandRecursively(Expansion<A, A> def) {
            this.expansions.add(recursive(def));
            return this;
        }

        public Expander<A, B> expandTypedExactly(Class<?> type, Expansion<A, B> def) {
            return expand(c -> c.getClass() == type ? def.expand(c) : null);
        }

        public Expander<A, B> expandTypedExactlyRecursive(Class<?> type, Expansion<A, A> def) {
            return expand(c -> {
                if (c.getClass() != type) return null;
                var expanded = def.expand(c);
                return expanded == null || expanded == c ? null : expand(expanded);
            });
        }

        public <Z> Expander<A, B> expandTyped(Class<Z> type, Expansion<Z, B> def) {
            return expand(c -> type.isInstance(c) ? def.expand((Z)c) : null);
        }

        public <Z> Expander<A, B> expandTypedRecursive(Class<Z> type, Expansion<Z, A> def) {
            return expand(c -> {
                if (!type.isInstance(c)) return null;
                var expanded = def.expand((Z)c);
                return expanded == null || expanded == c ? null : expand(expanded);
            });
        }

        private Expansion<A, B> recursive(Expansion<A, A> expansion) {
            return input -> {
                var expanded = expansion.expand(input);
                return expanded == null || expanded == input ? null : expand(expanded);
            };
        }

        public Expander<A, B> extractField(Class<?> type, String name) {
            return expandTypedExactly(type, CodecDecompositionUtil.extractField(type, name));
        }

        public Expander<A, B> extractFieldRecursive(Class<?> type, String name) {
            return expandTypedExactlyRecursive(type, CodecDecompositionUtil.extractField(type, name));
        }

        public Expander<A, B> extractAnonymousThis(Class<?>... types) {
            for (final var type : types) {
                extractFieldRecursive(type, "this$0");
            }
            return this;
        }

        public B expand(A input) throws Throwable {
            for (var def : expansions) {
                var expanded = def.expand(input);
                if (expanded != null) {
                    return expanded;
                }
            }
            return fallback.apply(input);
        }

        public interface Expansion<A, B> {
            @Nullable
            B expand(A input) throws Throwable;
        }
    }

    @SuppressWarnings("Anonymous2MethodRef") // We need a decoder instance, and if we fold the IDE will suggest `Codec.STRING`
    private static final Decoder<String> EXAMPLE_DECODER = new Decoder<>() {
        @Override
        public <T> DataResult<Pair<String, T>> decode(DynamicOps<T> ops, T input) {
            return Codec.STRING.decode(ops, input);
        }
    };
    private static final MapDecoder<String> EXAMPLE_MAP_DECODER = EXAMPLE_DECODER.fieldOf("example");
    @SuppressWarnings("Anonymous2MethodRef")
    private static final Encoder<String> EXAMPLE_ENCODER = new Encoder<>() {
        @Override
        public <T> DataResult<T> encode(String input, DynamicOps<T> ops, T prefix) {
            return Codec.STRING.encode(input, ops, prefix);
        }
    };
    private static final MapEncoder<String> EXAMPLE_MAP_ENCODER = EXAMPLE_ENCODER.fieldOf("example");

    public static final Expander<Encoder<?>, Encoder<?>> ENCODER_EXPANDER = new Expander<Encoder<?>, Encoder<?>>(Function.identity())
            .extractAnonymousThis(
                    EXAMPLE_ENCODER.<String>flatComap(DataResult::success).getClass(),
                    EXAMPLE_ENCODER.comap(Function.identity()).getClass(),
                    EXAMPLE_ENCODER.withLifecycle(Lifecycle.stable()).getClass()
            )
            .expandRecursively(expandEncoderFromCodec());

    public static final Expander<Decoder<?>, Decoder<?>> DECODER_EXPANDER = new Expander<Decoder<?>, Decoder<?>>(Function.identity())
            .extractAnonymousThis(
                    EXAMPLE_DECODER.flatMap(DataResult::success).getClass(),
                    EXAMPLE_DECODER.map(Function.identity()).getClass(),
                    EXAMPLE_DECODER.withLifecycle(Lifecycle.stable()).getClass(),
                    EXAMPLE_DECODER.promotePartial(err -> {}).getClass()
            )
            .expandRecursively(expandDecoderFromCodec());

    public record EncoderDecoder(Encoder<?> encoder, Decoder<?> decoder) {}

    public static final Expander<Codec<?>, Codec<?>> CODEC_EXPANDER = Expander.<Codec<?>>identity()
            .extractFieldRecursive(
                    ExtraCodecs.orCompressed(Codec.STRING, Codec.STRING).getClass(),
                    "val$pFirst" // TODO - this depends on mappings
            )
            .extractAnonymousThis(
                    Codec.STRING.mapResult(null).getClass(),
                    Codec.STRING.withLifecycle(Lifecycle.stable()).getClass()
            );

    public static final Expander<Codec<?>, EncoderDecoder> CODEC_EXTRACTOR = new Expander<Codec<?>, EncoderDecoder>(e -> new EncoderDecoder(e, e))
            .expandRecursively(CODEC_EXPANDER::expand)
            .expandTypedExactly(
                    Codec.of(EXAMPLE_ENCODER, EXAMPLE_DECODER).getClass(),
                    input -> new EncoderDecoder(ENCODER_EXPANDER.expand(extractEncoder(input)), DECODER_EXPANDER.expand(extractDecoder(input)))
            );

    public static final Expander<MapDecoder<?>, MapDecoder<?>> MAP_DECODER_EXPANDER = Expander.<MapDecoder<?>>identity()
            .extractAnonymousThis(
                EXAMPLE_MAP_DECODER.map(Function.identity()).getClass(),
                EXAMPLE_MAP_DECODER.flatMap(DataResult::success).getClass(),
                EXAMPLE_MAP_DECODER.withLifecycle(Lifecycle.stable()).getClass()
            );

    public static final Expander<MapEncoder<?>, MapEncoder<?>> MAP_ENCODER_EXPANDER = Expander.<MapEncoder<?>>identity()
            .extractAnonymousThis(
                EXAMPLE_MAP_ENCODER.comap(Function.identity()).getClass(),
                EXAMPLE_MAP_ENCODER.<String>flatComap(DataResult::success).getClass(),
                EXAMPLE_MAP_ENCODER.withLifecycle(Lifecycle.stable()).getClass()
            );

    public static final VarHandle FIELD_DECODER_NAME = Reflections.fieldHandle(FieldDecoder.class, "name");
    public static final VarHandle FIELD_DECODER_ELEMENT_CODEC = Reflections.fieldHandle(FieldDecoder.class, "elementCodec");

    public static final VarHandle OPTIONAL_FIELD_CODEC_NAME = Reflections.fieldHandle(OptionalFieldCodec.class, "name");
    public static final VarHandle OPTIONAL_FIELD_CODEC_ELEMENT_CODEC = Reflections.fieldHandle(OptionalFieldCodec.class, "elementCodec");

    public static final Expander<MapDecoder<?>, FieldInformation<Decoder<?>>> MAP_DECODER_TO_FIELD = new Expander<MapDecoder<?>, FieldInformation<Decoder<?>>>(e -> null)
            .expandTyped(OptionalFieldCodec.class, input -> new FieldInformation<>(
                    (String) OPTIONAL_FIELD_CODEC_NAME.get(input), DECODER_EXPANDER.expand((Decoder<?>) OPTIONAL_FIELD_CODEC_ELEMENT_CODEC.get(input)), true
            ))
            .expandTypedExactly(
                    ExtraCodecs.strictOptionalField(Codec.STRING, "example").getClass(),
                    decoder -> new FieldInformation<>(Reflections.getField("name", decoder), Reflections.getField("elementCodec", decoder), true)
            )

            // This should be resolved before optionalfieldcodec which also extends from mapcodec
            .expandTypedRecursive(MapCodec.class, codec -> RCBDecomposing.decompose(codec).decoder())
            .expandTyped(FieldDecoder.class, decoder -> new FieldInformation<>(
                    (String) FIELD_DECODER_NAME.get(decoder), (Decoder<?>) FIELD_DECODER_ELEMENT_CODEC.get(decoder), false
            ))

            .expandRecursively(MAP_DECODER_EXPANDER::expand);

    private static Decoder<?> extractDecoder(Codec<?> codec) {
        return Reflections.getField("val$decoder", codec);
    }
    private static Encoder<?> extractEncoder(Codec<?> codec) {
        return Reflections.getField("val$encoder", codec);
    }

    private static Expander.Expansion<Decoder<?>, Decoder<?>> expandDecoderFromCodec() {
        return input -> input instanceof Codec<?> c ? CODEC_EXTRACTOR.expand(c).decoder() : null;
    }

    private static Expander.Expansion<Encoder<?>, Encoder<?>> expandEncoderFromCodec() {
        return input -> input instanceof Codec<?> c ? CODEC_EXTRACTOR.expand(c).encoder() : null;
    }

    private static <A, B> Expander.Expansion<A, B> extractField(Class<?> type, String name) {
        try {
            final var field = type.getDeclaredField(name);
            final var offset = Reflections.UNSAFE.objectFieldOffset(field);
            return input -> (B) Reflections.UNSAFE.getObject(input, offset);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public record FieldInformation<T>(String name, T component, boolean optional) {}


    public static final Class<?> UNIT = Decoder.unit(null).getClass();
    public static boolean isUnit(MapDecoder<?> decoder) {
        return decoder.getClass() == UNIT;
    }
}
