package fr.cla.jam.apitypes.callback.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.atomic.AtomicInteger;

public class QuasarCallbackCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public QuasarCallbackCfJenkinsPlugin(CallbackJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        super(
            srv,
            CallbackCfAdapter.adapt(srv::findBundlesByName),
            bundles -> CallbackCfAdapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }


//    @Override public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
//        Fiber<Set<JiraComponent>> f = new Fiber<>(dedicatedScheduler, () ->
//            new CfFiberAsync<>(bundleName, findComponentsByBundleNameAsync).run()
//        ).start();
//
//        try {
//            return f.get();
//        } catch (ExecutionException e) {
//            Throwable cause = e.getCause();
//            if(cause instanceof CompletionException) throw (CompletionException)cause;
//            throw new RuntimeException(cause);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return emptySet();
//        }
//    }

}
