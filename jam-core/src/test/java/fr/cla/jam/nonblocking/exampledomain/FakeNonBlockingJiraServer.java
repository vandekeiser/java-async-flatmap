package fr.cla.jam.nonblocking.exampledomain;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.completionstage.NonBlockingJiraServer;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toSet;

public class FakeNonBlockingJiraServer implements NonBlockingJiraServer {
    public static final int 
            NB_OF_BUNDLES_PER_NAME = 100,
            NB_OF_COMPONENTS_PER_BUNDLE = 3;

    @Override
    public CompletableFuture<Set<JiraBundle>> findBundlesByName(String bundleName) {
        String nonNullBundleName = requireNonNull(bundleName);
        return completedFuture(
            IntStream.range(0, NB_OF_BUNDLES_PER_NAME)
                .mapToObj(i -> new JiraBundle(nonNullBundleName + i))
                .collect(toSet())
        );
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        JiraBundle nonNullBundle = requireNonNull(bundle);
        return completedFuture(
            IntStream.range(0, NB_OF_COMPONENTS_PER_BUNDLE)
                .mapToObj(i -> new JiraComponent(nonNullBundle.toString() + i))
                .collect(toSet())
        );
    }
    
}
