package fr.cla.jam;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Functions {

    public static <A1, A2, R> Function<
        BiFunction<A1, A2, R>,
        Function<A2, R>
    > curry(A1 arg1) {
        return uncurried -> arg2 -> uncurried.apply(arg1, arg2);
    }

}
