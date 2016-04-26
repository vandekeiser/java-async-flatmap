package fr.cla.jam;

import org.junit.Test;

import java.util.Set;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

//deepneural4j

public class CsfTest {

    private static final String[] NAMES = {"toto", "tata", "titi"};
    private static final Csf<String> FUTURE_NAMES = stringCsf(NAMES);

    @Test
    public void should_flatmap() {
        Csf<String> futureBisNames = FUTURE_NAMES.flatMap(name -> stringCsf(
            name,
            name + "bis"
        ));
        
        Set<String> bisNames = futureBisNames.join();
        assertThat(bisNames).hasSize(2 * NAMES.length);
    }
    
    @Test
    public void should_flatmap_twice() {
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

    
    private static Csf<String> stringCsf(String... values) {
        return Csf.ofSuccess(String.class, values);
    }
    
}
