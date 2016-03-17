package cla.completablefuture.nonblocking.callback;

import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;
import cla.completablefuture.nonblocking.callback.exampledomain.CallbackJiraServer;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class FakeCallbackJiraServer implements CallbackJiraServer {
    public static final int 
            NB_OF_BUNDLES_PER_NAME = 100,
            NB_OF_COMPONENTS_PER_BUNDLE = 3;

    @Override
    public Callback<Set<JiraBundle>> findBundlesByName(String bundleName) {
        CompletableCallback<Set<JiraBundle>> ret = new BasicCompletableCallback<>();
        ret.complete(
            IntStream.range(0, NB_OF_BUNDLES_PER_NAME)
                .mapToObj(i -> new JiraBundle())
                .collect(toSet())
        );
        return ret;
    }

    @Override
    public Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        CompletableCallback<Set<JiraComponent>> ret = new BasicCompletableCallback<>();
        ret.complete(
            IntStream.range(0, NB_OF_COMPONENTS_PER_BUNDLE)
                .mapToObj(i -> new JiraComponent())
                .collect(toSet())
        );
        return ret;
    }
    
}
