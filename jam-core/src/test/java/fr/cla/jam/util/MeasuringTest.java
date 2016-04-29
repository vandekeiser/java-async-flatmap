package fr.cla.jam.util;

import fr.cla.jam.exampledomain.JenkinsPlugin;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.ForkJoinPool.commonPool;

public abstract class MeasuringTest {

    protected void printEnv(PrintStream out, Executor pool) {
        out.printf(
            "----------------------------------CORES=%d, FJP SIZE=%d, DEDICATED POOL SIZE=%d-----------------------------------%n",
            getRuntime().availableProcessors(),
            commonPool().getParallelism(),
            size(pool)
        );
    }

    protected void printEnv(PrintStream oout, Executor pool, int concurrency) {
        oout.printf(
                "-----------------------------CORES=%d, FJP SIZE=%d, DEDICATED POOL SIZE=%d, CONCURRENCY=%d------------------------------%n",
                getRuntime().availableProcessors(),
                commonPool().getParallelism(),
                size(pool),
                concurrency
        );
    }

    protected void printResult(PrintStream out, JenkinsPlugin sut, Instant beforeCall, Collection<?> answers) {
        out.printf(
            "%-90s took %-12s (found %d) %n",
            sut + ":",
            Duration.between(beforeCall, Instant.now()),
            answers.size()
        );
    }

    private int size(Executor pool) {
        return ((ThreadPoolExecutor) pool).getMaximumPoolSize();
    }

}
