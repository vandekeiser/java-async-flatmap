package cla.completablefuture;

import java.util.HashSet;
import java.util.Set;

public final class Sets {
    
    public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
        Set<E> u = new HashSet<>(s1);
        u.addAll(s2);
        return u;
    }
    
}
