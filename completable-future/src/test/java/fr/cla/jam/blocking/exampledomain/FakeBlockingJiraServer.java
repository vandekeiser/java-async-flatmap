package fr.cla.jam.blocking.exampledomain;

import java.util.Set;
import java.util.stream.IntStream;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import static java.util.stream.Collectors.toSet;

public class FakeBlockingJiraServer implements BlockingJiraServer {
    public static final int 
            NB_OF_BUNDLES_PER_NAME = 100,
            NB_OF_COMPONENTS_PER_BUNDLE = 3;

    @Override
    public Set<JiraBundle> findBundlesByName(String bundleName) {
        return IntStream.range(0, NB_OF_BUNDLES_PER_NAME)
            .mapToObj(i -> new JiraBundle())
            .collect(toSet());
    }

    @Override
    public Set<JiraComponent> findComponentsByBundle(JiraBundle bundle) {
        return IntStream.range(0, NB_OF_COMPONENTS_PER_BUNDLE)
            .mapToObj(i -> new JiraComponent())
            .collect(toSet());
    }
}