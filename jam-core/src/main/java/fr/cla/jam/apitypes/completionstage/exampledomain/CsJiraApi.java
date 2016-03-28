package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.exampledomain.JiraApi;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface CsJiraApi extends JiraApi {
    
    CompletionStage<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    CompletionStage<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);

}
