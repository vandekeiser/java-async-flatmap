package fr.cla.jam;

import fr.cla.jam.util.containers.Sets;
import org.junit.Test;

import java.util.Set;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

//deepneural4j

public class CsfTest {

    private static final String[] 
        NAMES = {"toto", "tata", "titi"},
        MORE_NAMES = {"alpha", "bravo", "charlie"};
    private static final Csf<String> 
        FUTURE_NAMES = stringCsf(NAMES),
        MORE_FUTURE_NAMES = stringCsf(MORE_NAMES);

    @Test public void should_flatmap() {
        Csf<String> futureBisNames = FUTURE_NAMES.flatMap(name -> stringCsf(
            name,
            name + "bis"
        ));
        
        Set<String> bisNames = futureBisNames.join();
        assertThat(bisNames).hasSize(2 * NAMES.length);
    }
    
    @Test public void should_flatmap_twice() {
        Csf<String> futureBisNames = FUTURE_NAMES
            .flatMap(name -> stringCsf(
                name,
                name + "bis"
            ))
            .flatMap(name -> stringCsf(
                    name + "1",
                    name + "2",
                    name + "3"
            ));
        
        Set<String> bisNames = futureBisNames.join();
        assertThat(bisNames).hasSize(3 * 2 * NAMES.length);
    }

    @Test public void should_filter() {
        Csf<String> futureNamesContainingA = FUTURE_NAMES.filter(
                name -> name.contains("a")
        );
        
        Set<String> namesContainingA = futureNamesContainingA.join();
        
        assertThat(namesContainingA).containsOnly("tata");
    }
    
    @Test public void should_map() {
        Csf<String> futureNamesPlus2 = FUTURE_NAMES.map(
            name -> name + "2"
        );
        
        Set<String> namesContainingA = futureNamesPlus2.join();
        
        assertThat(namesContainingA).containsOnly("toto2", "tata2", "titi2");
    }
    
    @Test public void should_concat() {
        Csf<String> futureAllNames = FUTURE_NAMES.concat(MORE_FUTURE_NAMES);
        
        Set<String> allNames = futureAllNames.join();
        
        assertThat(allNames).hasSize(NAMES.length + MORE_NAMES.length);
    }
    
    private static Csf<String> stringCsf(String... values) {
        return Csf.of(String.class, values);
    }
    
}
