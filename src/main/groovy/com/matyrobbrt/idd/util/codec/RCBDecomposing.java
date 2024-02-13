package com.matyrobbrt.idd.util.codec;

import com.matyrobbrt.idd.util.Reflections;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class RCBDecomposing {
    private static final RecordCodecBuilder<?, ?> JUST_ONE_RCB = (RecordCodecBuilder<?, ? extends Object>) RecordCodecBuilder.instance()
            .lift1(Reflections.allocate(RecordCodecBuilder.class)).apply(Reflections.allocate(RecordCodecBuilder.class));
    public static final Class<?> JUST_ONE = getUnderlyingDecoder(JUST_ONE_RCB).getClass();

    private static final RecordCodecBuilder<?, ?> JUST_TWO_RCB = (RecordCodecBuilder<?, ? extends Object>) RecordCodecBuilder.instance()
            .ap2(Reflections.allocate(RecordCodecBuilder.class), Reflections.allocate(RecordCodecBuilder.class), Reflections.allocate(RecordCodecBuilder.class));
    public static final Class<?> JUST_TWO = getUnderlyingDecoder(JUST_TWO_RCB).getClass();

    public static final Class<?> UNIT_DECODER = Decoder.unit(() -> null).getClass();

    public static final Class<?> MAP_CODEC_OF = MapCodec.of(Codec.STRING.fieldOf(""), Codec.STRING.fieldOf("")).getClass();
    public static final Class<?> MAP_CODEC_CODEC_OF = Codec.of(Codec.STRING.fieldOf(""), Codec.STRING.fieldOf("")).getClass();

    public static <T> RecordCodecBuilder<T, ?> getUnderlying(Codec<T> codec) {
        return (RecordCodecBuilder) Reflections.WRAPPED_BUILDER.get(((MapCodec.MapCodecCodec) codec).codec());
    }

    public static <F> MapDecoder<F> getUnderlyingDecoder(RecordCodecBuilder<?, F> recordCodecBuilder) {
        return Reflections.getField("decoder", recordCodecBuilder);
    }

    public static <O> Codec<O> getElementCodec(ListCodec<O> codec) {
        return Reflections.getField("elementCodec", codec);
    }

    public record MapCodecInfo<A>(MapEncoder<A> encoder, MapDecoder<A> decoder) {}
    public static <A> MapCodecInfo<A> decompose(MapCodec<A> codec) {
        if (codec.getClass() == MAP_CODEC_OF || codec.getClass() == MAP_CODEC_CODEC_OF) {
            return new MapCodecInfo<>(
                    Reflections.getField("val$encoder", codec),
                    Reflections.getField("val$decoder", codec)
            );
        }
        throw null;
    }

    public static <A> Stream<RecordCodecBuilder<?, ?>> decomposeRecursively(RecordCodecBuilder<?, A> root) {
        return decomposeRecursively(root, null);
    }

    public static <A> Stream<RecordCodecBuilder<?, ?>> decomposeRecursively(RecordCodecBuilder<?, A> root, Consumer<Object> functionFound) {
        final Stream.Builder<RecordCodecBuilder<?, ?>> stream = Stream.builder();

        try {
            final var decoder = getUnderlyingDecoder(root);
            final var clz = decoder.getClass();
            final var funcField = Reflections.fieldOrNull(clz, "val$function");

            if (clz == JUST_ONE) {
                final var fField = Reflections.fieldOrNull(clz, "val$a");
                fField.setAccessible(true);
                final var rcb = (RecordCodecBuilder<?, ?>) fField.get(decoder);
                accept(stream, rcb, null);
            } else if (clz == JUST_TWO) {
                for (char i = 'a'; i <= 'b'; i++) {
                    final var fField = Reflections.fieldOrNull(clz, "val$f" + i);
                    fField.setAccessible(true);
                    final var rcb = (RecordCodecBuilder<?, ?>) fField.get(decoder);
                    accept(stream, rcb, null);
                }

                if (functionFound != null) {
                    functionFound.accept(getUnderlyingDecoder(Reflections.getField("val$function", decoder)).decode(null, null).result().orElseThrow());
                }
            } else if (funcField != null) {
                {
                    funcField.setAccessible(true);
                    final var rcb = (RecordCodecBuilder<?, ?>) funcField.get(decoder);

                    final var under = RCBDecomposing.getUnderlyingDecoder(rcb);
                    final var thisField = Reflections.fieldOrNull(under.getClass(), "this$0");
                    if (thisField != null) {
                        thisField.setAccessible(true);
                        final var u = thisField.get(under);
                        if (u.getClass() == UNIT_DECODER && functionFound != null) {
                            functionFound.accept(((MapDecoder<Object>) u).decode(null, null).result().orElseThrow());
                        }
                    }
                    decomposeRecursively(rcb, functionFound).forEach(stream);
                }

                for (int i = 1; i <= 4; i++) {
                    final var fField =Reflections.fieldOrNull(clz, "val$f" + i);
                    if (fField != null) {
                        fField.setAccessible(true);
                        final var rcb = (RecordCodecBuilder<?, ?>) fField.get(decoder);
                        accept(stream, rcb, null);
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception exception) {
            return stream.build();
        }

        return stream.build();
    }

    private static void accept(Stream.Builder<RecordCodecBuilder<?, ?>> stream, RecordCodecBuilder<?, ?> rcb, Consumer<Object> functionFound) {
        final var recursive = decomposeRecursively(rcb, functionFound).toList();
        if (!recursive.isEmpty()) {
            recursive.forEach(stream);
        } else {
            stream.accept(rcb);
        }
    }
}
