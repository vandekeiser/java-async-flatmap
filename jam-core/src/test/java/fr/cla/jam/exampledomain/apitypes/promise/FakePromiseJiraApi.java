package fr.cla.jam.exampledomain.apitypes.promise;

import fr.cla.jam.apitypes.promise.CompletablePromise;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.util.FakeApi;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class FakePromiseJiraApi implements PromiseJiraApi {

    @Override
    public Promise<Set<JiraBundle>> findBundlesByName(String bundleName) {
        String nonNullBundleName = requireNonNull(bundleName);
        CompletablePromise<Set<JiraBundle>> ret = CompletablePromise.basic();

        ret.complete(
            IntStream.range(0, FakeApi.NB_OF_BUNDLES_PER_NAME)
                .mapToObj(i -> new JiraBundle(nonNullBundleName + i))
                .collect(toSet())
        );
        return ret;
    }

    @Override
    public Promise<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        JiraBundle nonNullBundle = requireNonNull(bundle);
        CompletablePromise<Set<JiraComponent>> ret = CompletablePromise.basic();

        ret.complete(
            IntStream.range(0, FakeApi.NB_OF_COMPONENTS_PER_BUNDLE)
                .mapToObj(i -> new JiraComponent(nonNullBundle.toString() + i))
                .collect(toSet())
        );
        return ret;
    }

    @Override public String description() {
        return getClass().getSimpleName();
    }

}
