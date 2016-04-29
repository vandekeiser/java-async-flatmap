package fr.cla.jam;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

//deepneural4j

public class CsfTest {

    private static final String[] 
        NAMES = {"toto", "tata", "titi"},
        MORE_NAMES = {"alpha", "bravo", "charlie"};
    private static final CfOfSet<String> 
        FUTURE_NAMES = stringCsf(NAMES),
        MORE_FUTURE_NAMES = stringCsf(MORE_NAMES);

    @Test public void should_flatmap() {
        CfOfSet<String> futureBisNames = FUTURE_NAMES.flatMap(name -> stringCsf(
            name, name + "bis"
        ));
        
        Set<String> bisNames = futureBisNames.join();
        assertThat(bisNames).hasSize(2 * NAMES.length);
    }
    
    @Test public void should_flatmap_twice() {
        CfOfSet<String> futureBisNames = FUTURE_NAMES
            .flatMap(name -> stringCsf(
                name, name + "bis"
            ))
            .flatMap(name -> stringCsf(
                name + "1", name + "2", name + "3"
            ));
        
        Set<String> bisNames = futureBisNames.join();
        assertThat(bisNames).hasSize(3 * 2 * NAMES.length);
    }

    @Test public void should_filter() {
        CfOfSet<String> futureNamesContainingA = FUTURE_NAMES.filter(
            name -> name.contains("a")
        );
        
        Set<String> namesContainingA = futureNamesContainingA.join();
        
        assertThat(namesContainingA).containsOnly("tata");
    }
    
    @Test public void should_map() {
        CfOfSet<String> futureNamesPlus2 = FUTURE_NAMES.map(
            name -> name + "2"
        );
        
        Set<String> namesContainingA = futureNamesPlus2.join();
        
        assertThat(namesContainingA).containsOnly("toto2", "tata2", "titi2");
    }
    
    @Test public void should_concat() {
        CfOfSet<String> futureAllNames = FUTURE_NAMES.concat(MORE_FUTURE_NAMES);
        
        Set<String> allNames = futureAllNames.join();
        
        assertThat(allNames).hasSize(NAMES.length + MORE_NAMES.length);
    }
    
    private static CfOfSet<String> stringCsf(String... values) {
        return CfOfSet.of(String.class, values);
    }
    
}
