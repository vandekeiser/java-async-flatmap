package fr.cla.jam.util.containers;

import java.util.HashSet;
import java.util.Set;

public final class Sets {
    
    public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2) {
        Set<E> u = new HashSet<>(s1);
        u.addAll(s2);
        return u;
    }
    
}
