package fr.cla.jam.nonblocking.completionstage;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface CompletionStageJiraApi {
    
    CompletionStage<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    CompletionStage<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
 
}
