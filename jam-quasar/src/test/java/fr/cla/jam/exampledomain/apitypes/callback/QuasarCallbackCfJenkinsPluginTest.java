package fr.cla.jam.exampledomain.apitypes.callback;

import fr.cla.jam.exampledomain.AbstractQuasarJenkinsPluginTest;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

//deepneural4j

//Run with
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false -XX:+HeapDumpOnOutOfMemoryError -mx2G
// -javaagent:"C:\Users\User\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false -XX:+HeapDumpOnOutOfMemoryError -mx2G

//TODO:
// -tester scalabilite avec JMH
// -factor nb de resultats
// -separer modules fwk et domain
public class QuasarCallbackCfJenkinsPluginTest extends AbstractQuasarJenkinsPluginTest {

    @Override
    protected CsfJenkinsPlugin defectiveSut() {
        return new QuasarCallbackCfJenkinsPlugin(new DefectiveCallbackJiraApi(), dedicatedScheduler(latencyMeasurementPool));
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        return new QuasarCallbackCfJenkinsPlugin(new HalfDefectiveCallbackJiraApi(), dedicatedScheduler(latencyMeasurementPool));
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new QuasarCallbackCfJenkinsPlugin(
            new NonBlockingLatentCallbackJiraApi(new FakeCallbackJiraApi()),
            dedicatedScheduler(latencyMeasurementPool)
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 1;
    }
//-XX:+HeapDumpOnOutOfMemoryError -javaagent:"C:\Users\User\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
//-XX:+HeapDumpOnOutOfMemoryError
//    java.lang.OutOfMemoryError: GC overhead limit exceeded
//    Dumping heap to java_pid46936.hprof ...
//    Heap dump file created [1284154277 bytes in 79.619 secs]

    @Override
    protected int scalabilityTestConcurrency() {
        return 10;
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        CallbackJiraApi nonBlockingCallbackApi = new NonBlockingLatentCallbackJiraApi(new FakeCallbackJiraApi());

        return Arrays.asList(
            new CallbackCfJenkinsPlugin(nonBlockingCallbackApi),
            new QuasarCallbackCfJenkinsPlugin(nonBlockingCallbackApi, dedicatedScheduler(measurementPool))
        );
    }


    /*
    * AVEC -javaagent
    *   blockingCallbackApi + CallbackCfJenkinsPlugin = jamais
    *   blockingCallbackApi + QuasarCallbackCfJenkinsPlugin = jamais
    *   nonBlockingCallbackApi + CallbackCfJenkinsPlugin = PT6.957S
    *   nonBlockingCallbackApi + QuasarCallbackCfJenkinsPlugin = PT10.04S
    *
    * SANS -javaagent
    *   blockingCallbackApi + CallbackCfJenkinsPlugin = jamais
    *   blockingCallbackApi + QuasarCallbackCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *   nonBlockingCallbackApi + CallbackCfJenkinsPlugin = "QUASAR WARNING" , PT8.659S
    *   nonBlockingCallbackApi + QuasarCallbackCfJenkinsPlugin = "QUASAR WARNING" , PT6.268S
    *
    * -->la presence de l'agent ne change rien!!
    * -->l'utilisation de l'api blocking/nonblocking fait toute la difference
    *   -->mais ca devrait etre impossible puisque les Fiber.sleep devbraient devenir des Thread.sleep
    *
    * SANS -javaagent, remplace fiber.sleep par thread.sleep dans nonBlockingCallbackApi
    *   nonBlockingCallbackApi(thread.sleep) + CallbackCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCallbackCfJenkinsPlugin = "QUASAR WARNING" , jamais
    * donc c'est bien ca
    *
    * Par contre, il y avait un truc bizarre ds AbstractLatentCallbackJiraApi:
    *   protected static final Executor delayExecutor = Executors.newFixedThreadPool(1);
    * On change juste ca, tjrs avec un sleep ds le supposement nonblocking:
    *   protected static final Executor delayExecutor = Executors.newCachedThreadPool();
    *
    * Ca donne:
    *   nonBlockingCallbackApi(thread.sleep) + CallbackCfJenkinsPlugin = "QUASAR WARNING" ,  PT7.197S
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCallbackCfJenkinsPlugin = "QUASAR WARNING" ,  PT14.858S
    *
    * C'est uniquement le ralentissement du sleep qui cause ca
    * On passe CONCURRENCY à 10_000 pour voir si l'api cesse d'etre capable d'etre nonblocking et que ca s'effondre qd on depasse le nb de threads possibles:
    *
    *Ca donne:
    *   nonBlockingCallbackApi(thread.sleep) + CallbackCfJenkinsPlugin = "QUASAR WARNING" , PT1M34.403S
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCallbackCfJenkinsPlugin = "QUASAR WARNING" , PT3M57.857S
    *
    * Je repasse en fiber.sleep pour voir si ca arrange (le delay executor est tjrs newCached):
    *   nonBlockingCallbackApi(thread.sleep) + CallbackCfJenkinsPlugin = "QUASAR WARNING" , PT1M50.697S
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCallbackCfJenkinsPlugin = "QUASAR WARNING" , PT2M9.132S
    *Ca change rien..
    *
    * Je remet l'agent:
    *   nonBlockingCallbackApi(thread.sleep) + CallbackCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCallbackCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *
    * Je remet le delaypool a 1, en fait son but etait justement d'eviter ca:
    * (CallbackCfJenkinsPlugin)
    *   -avec l'agent c'est long, mais on voit defiler les after xxx a 13s, puis OutOfMemoryError: GC overhead limit exceeded
     *  -sans l'agent c'est long, mais on voit defiler les after xxx a 1mn, puis OutOfMemoryError: GC overhead limit exceeded
     *
     * *  Exception in thread "pool-6-thread-3709" java.util.concurrent.CompletionException: java.lang.OutOfMemoryError: GC overhead limit exceeded
	at java.util.concurrent.CompletableFuture.internalComplete(CompletableFuture.java:205)
	at java.util.concurrent.CompletableFuture$ThenCompose.run(CompletableFuture.java:1487)
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:193)
	at java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2345)
	at fr.cla.jam.apitypes.callback.CallbackApi2CfApi$1.onSuccess(CallbackApi2CfApi.java:25)
	at fr.cla.jam.exampledomain.apitypes.callback.NonBlockingLatentCallbackJiraApi.lambda$sleepThenPropagateSuccess$bf9cda6e$1(NonBlockingLatentCallbackJiraApi.java:26)
	at fr.cla.jam.exampledomain.apitypes.callback.NonBlockingLatentCallbackJiraApi$$Lambda$25/500448553.run(Unknown Source)
	at co.paralleluniverse.strands.SuspendableUtils$VoidSuspendableCallable.run(SuspendableUtils.java:44)
	at co.paralleluniverse.strands.SuspendableUtils$VoidSuspendableCallable.run(SuspendableUtils.java:32)
	at co.paralleluniverse.fibers.Fiber.run(Fiber.java:1026)
	at co.paralleluniverse.fibers.Fiber.run1(Fiber.java:1021)
	at co.paralleluniverse.fibers.Fiber.exec(Fiber.java:732)
	at co.paralleluniverse.fibers.RunnableFiberTask.doExec(RunnableFiberTask.java:94)
	at co.paralleluniverse.fibers.RunnableFiberTask.run(RunnableFiberTask.java:85)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
Caused by: java.lang.OutOfMemoryError: GC overhead limit exceeded
	at java.util.Arrays.copyOf(Arrays.java:3332)
	at java.lang.AbstractStringBuilder.expandCapacity(AbstractStringBuilder.java:137)
	at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:121)
	at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:421)
	at java.lang.StringBuilder.append(StringBuilder.java:136)
	at co.paralleluniverse.fibers.Fiber.setName(Fiber.java:287)
	at co.paralleluniverse.fibers.Fiber.<init>(Fiber.java:172)
	at co.paralleluniverse.fibers.Fiber.<init>(Fiber.java:348)
	at co.paralleluniverse.fibers.Fiber.<init>(Fiber.java:375)
	at fr.cla.jam.exampledomain.apitypes.callback.NonBlockingLatentCallbackJiraApi.sleepThenPropagateSuccess(NonBlockingLatentCallbackJiraApi.java:23)
	at fr.cla.jam.apitypes.callback.exampledomain.AbstractLatentCallbackJiraApi$1.onSuccess(AbstractLatentCallbackJiraApi.java:49)
	at fr.cla.jam.apitypes.callback.exampledomain.FakeCallbackJiraApi.findComponentsByBundle(FakeCallbackJiraApi.java:30)
	at fr.cla.jam.apitypes.callback.exampledomain.AbstractLatentCallbackJiraApi$$Lambda$28/209128072.accept(Unknown Source)
	at fr.cla.jam.apitypes.callback.exampledomain.AbstractLatentCallbackJiraApi.lambda$delay$0(AbstractLatentCallbackJiraApi.java:46)
	at fr.cla.jam.apitypes.callback.exampledomain.AbstractLatentCallbackJiraApi$$Lambda$20/1918461242.accept(Unknown Source)
	at fr.cla.jam.apitypes.callback.exampledomain.AbstractLatentCallbackJiraApi.findComponentsByBundle(AbstractLatentCallbackJiraApi.java:41)
	at fr.cla.jam.apitypes.callback.exampledomain.CallbackCfJenkinsPlugin$$Lambda$27/1568587390.accept(Unknown Source)
	at fr.cla.jam.apitypes.callback.CallbackApi2CfApi.cfThatWaitsToBeCalledBack(CallbackApi2CfApi.java:22)
	at fr.cla.jam.apitypes.callback.CallbackApi2CfApi.lambda$null$47(CallbackApi2CfApi.java:13)
	at fr.cla.jam.apitypes.callback.CallbackApi2CfApi$$Lambda$12/1325808650.apply(Unknown Source)
	at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)
	at java.util.HashMap$KeySpliterator.forEachRemaining(HashMap.java:1540)
	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:512)
	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:502)
	at java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:708)
	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
	at java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:499)
	at fr.cla.jam.apitypes.callback.CallbackCfAdapter.flatMapAdapt(CallbackCfAdapter.java:23)
	at fr.cla.jam.apitypes.callback.exampledomain.CallbackCfJenkinsPlugin.lambda$new$28(CallbackCfJenkinsPlugin.java:25)
	at fr.cla.jam.apitypes.callback.exampledomain.CallbackCfJenkinsPlugin$$Lambda$13/510464020.apply(Unknown Source)
	at java.util.concurrent.CompletableFuture$ThenCompose.run(CompletableFuture.java:1453)
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:193)
     *
     *
     * idem avec QuasarCallbackCfJenkinsPlugin:
     *  OutOfMemoryError: GC overhead limit exceeded
     *  Exception in thread "Timer-0" Exception in thread "FiberTimedScheduler-default-fiber-pool" Exception in thread "pool-7-thread-7020" java.lang.OutOfMemoryError: GC overhead limit exceeded
	at java.util.TimerTask.<init>(TimerTask.java:40)
	at javax.management.timer.TimerAlarmClock.<init>(TimerAlarmClock.java:50)
	at javax.management.timer.Timer.updateTimerTable(Timer.java:1105)
	at javax.management.timer.Timer.notifyAlarmClock(Timer.java:1202)
	at javax.management.timer.TimerAlarmClock.run(TimerAlarmClock.java:74)
	at java.util.TimerThread.mainLoop(Timer.java:555)
	at java.util.TimerThread.run(Timer.java:505)
     *
     *Faire un -XX: dump on oome?
     *
     *
     *avec un PARALLESIM de 10, ca change rien
     * avec un PARALLESIM de 100, ca change rien
     *
     *c'est pe les cf qui prenne la place et pas les thread?
     * -combien de threads?
     * -combien de fibers? utiliser les mbeans programmatiquement si le debug veut pas marcher
     * -analyser le dump
     *
     *
     *
     *

     *
    * */
}
