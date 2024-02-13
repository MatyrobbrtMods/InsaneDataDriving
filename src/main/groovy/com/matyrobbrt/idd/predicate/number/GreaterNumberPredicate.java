package com.matyrobbrt.idd.predicate.number;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public record GreaterNumberPredicate(Number number) implements NumberPredicate {
    public static final Codec<GreaterNumberPredicate> CODEC = Codec.DOUBLE
            .fieldOf("number")
            .<Number>xmap(Function.identity(), Number::doubleValue)
            .xmap(GreaterNumberPredicate::new, GreaterNumberPredicate::number)
            .codec();

    @Override
    public boolean test(Number number) {
        return number.doubleValue() > this.number.doubleValue();
    }

    @Override
    public Codec<? extends NumberPredicate> codec() {
        return CODEC;
    }
}
