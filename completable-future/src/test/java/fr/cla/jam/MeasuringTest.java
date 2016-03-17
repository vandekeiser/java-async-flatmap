package fr.cla.jam;

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

    protected void printResult(PrintStream out, JenkinsPlugin sut, Instant beforeCall, Collection<?> answers) {
        out.printf("%-90s took %-12s (found %d) %n", sut+":", Duration.between(beforeCall, Instant.now()), answers.size());
    }

    protected void printEnv(PrintStream out, Executor dedicatedPool) {
        out.printf(
                "----------------------------------CORES=%d, FJP SIZE=%d, DEDICATED POOL SIZE=%d-----------------------------------%n",
                getRuntime().availableProcessors(),
                commonPool().getParallelism(),
                dedicatedPoolSize(dedicatedPool)
        );
    }

    private int dedicatedPoolSize(Executor pool) {
        return ((ThreadPoolExecutor) pool).getMaximumPoolSize();
    }

}
