package com.matyrobbrt.idd.predicate.number;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public record EqualNumberPredicate(Number number) implements NumberPredicate {
    public static final Codec<EqualNumberPredicate> CODEC = Codec.DOUBLE
            .<Number>xmap(Function.identity(), Number::doubleValue)
            .fieldOf("number")
            .xmap(EqualNumberPredicate::new, EqualNumberPredicate::number)
            .codec();

    @Override
    public boolean test(Number number) {
        return this.number.equals(number);
    }

    @Override
    public Codec<? extends NumberPredicate> codec() {
        return CODEC;
    }
}
