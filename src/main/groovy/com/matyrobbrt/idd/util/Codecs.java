package com.matyrobbrt.idd.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.common.ToolAction;

import java.util.List;
import java.util.function.Function;

public class Codecs {
    enum IHand implements StringRepresentable {
        MAIN {
            @Override
            public String getSerializedName() {
                return "main_hand";
            }
        },
        OFF_HAND {
            @Override
            public String getSerializedName() {
                return "off_hand";
            }
        }
    }
    public static final Codec<InteractionHand> INTERACTION_HAND = StringRepresentable.fromEnum(IHand::values)
            .xmap(hand -> hand == IHand.MAIN ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, h -> h == InteractionHand.MAIN_HAND ? IHand.MAIN : IHand.OFF_HAND);

    public static <T> Codec<List<T>> listOf(Codec<T> codec) {
        return new ListOrSingleCodec<>(ExtraCodecs.either(
                codec,
                codec.listOf()
        ).xmap(e -> e.map(List::of, Function.identity()), v -> v.size() == 1 ? Either.left(v.get(0)) : Either.right(v)), codec);
    }

    public record ListOrSingleCodec<A>(Codec<List<A>> delegate, Codec<A> simpleCodec) implements Codec<List<A>> {

        @Override
        public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input) {
            return delegate.decode(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(List<A> input, DynamicOps<T> ops, T prefix) {
            return delegate.encode(input, ops, prefix);
        }
    }
}
