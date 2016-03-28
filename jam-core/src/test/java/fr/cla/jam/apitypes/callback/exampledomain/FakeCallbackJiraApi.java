package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.stream.IntStream;

import static fr.cla.jam.util.FakeApi.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.util.FakeApi.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class FakeCallbackJiraApi implements CallbackJiraApi {

    @Override
    public void findBundlesByName(String bundleName, Callback<Set<JiraBundle>> callback) {
        String nonNullBundleName = requireNonNull(bundleName);
        callback.onSuccess(
            IntStream.range(0, NB_OF_BUNDLES_PER_NAME)
                .mapToObj(i -> new JiraBundle(nonNullBundleName + i))
                .collect(toSet())
        );
    }

    @Override
    public void findComponentsByBundle(JiraBundle bundle, Callback<Set<JiraComponent>> callback) {
        JiraBundle nonNullBundle = requireNonNull(bundle);
        callback.onSuccess(
            IntStream.range(0, NB_OF_COMPONENTS_PER_BUNDLE)
                .mapToObj(i -> new JiraComponent(nonNullBundle.toString() + i))
                .collect(toSet())
        );
    }

    @Override public String description() {
        return getClass().getSimpleName();
    }

}
