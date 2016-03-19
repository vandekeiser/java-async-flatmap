package fr.cla.jam.nonblocking.callback;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.exampledomain.CallbackJiraServer;

import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class FakeCallbackJiraServer implements CallbackJiraServer {
    public static final int 
            NB_OF_BUNDLES_PER_NAME = 100,
            NB_OF_COMPONENTS_PER_BUNDLE = 3;

    @Override
    public Callback<Set<JiraBundle>> findBundlesByName(String bundleName) {
        String nonNullBundleName = Objects.requireNonNull(bundleName);
        CompletableCallback<Set<JiraBundle>> ret = CompletableCallback.basic();
        ret.complete(
            IntStream.range(0, NB_OF_BUNDLES_PER_NAME)
                .mapToObj(i -> new JiraBundle(nonNullBundleName + i))
                .collect(toSet())
        );
        return ret;
    }

    @Override
    public Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        JiraBundle nonNullBundle = Objects.requireNonNull(bundle);
        CompletableCallback<Set<JiraComponent>> ret = CompletableCallback.basic();
        ret.complete(
            IntStream.range(0, NB_OF_COMPONENTS_PER_BUNDLE)
                .mapToObj(i -> new JiraComponent(nonNullBundle.toString() + i))
                .collect(toSet())
        );
        return ret;
    }
    
}
