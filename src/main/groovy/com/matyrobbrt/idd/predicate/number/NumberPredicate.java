package com.matyrobbrt.idd.predicate.number;

import com.mojang.serialization.Codec;

public interface NumberPredicate {
    boolean test(Number number);

    Codec<? extends NumberPredicate> codec();
}
