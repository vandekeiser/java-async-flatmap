package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.stream.IntStream;

import static fr.cla.jam.FakeApi.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.FakeApi.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.util.stream.Collectors.toSet;

public class FakeSyncJiraApi implements SyncJiraApi {

    @Override
    public Set<JiraBundle> findBundlesByName(String bundleName) {
        return IntStream.range(0, NB_OF_BUNDLES_PER_NAME)
            .mapToObj(i -> new JiraBundle(bundleName + i))
            .collect(toSet());
    }

    @Override
    public Set<JiraComponent> findComponentsByBundle(JiraBundle bundle) {
        return IntStream.range(0, NB_OF_COMPONENTS_PER_BUNDLE)
            .mapToObj(i -> new JiraComponent(bundle.toString() + i))
            .collect(toSet());
    }
}
