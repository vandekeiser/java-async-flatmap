package cla.completablefuture.nonblocking.callback.exampledomain;

import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;
import cla.completablefuture.nonblocking.callback.Callback;

import java.util.Set;

public interface CallbackJiraServer {
    
    Callback<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
 
}
