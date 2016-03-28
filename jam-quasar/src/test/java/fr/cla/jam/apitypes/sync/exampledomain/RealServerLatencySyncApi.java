package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static fr.cla.jam.util.FakeApi.MAX_SLEEP;
import static fr.cla.jam.util.FakeApi.MIN_SLEEP;
import static java.lang.String.format;
import static java.lang.System.out;

public class RealServerLatencySyncApi implements SyncJiraApi {

    private static final BasicResponseHandler basicResponseHandler = new BasicResponseHandler();

    public static final int TIMEOUT = 30000;
    public static final int MAX_CONN = 120000;
    public static final int PORT = 8080;
    //See https://github.com/puniverse/CascadingFailureExample, it provided the code of blocking and non-blocking servers
    // pass -Dec2instance=...
    public static final String SERVICE_URL_TEMPLATE = "http://%s:%d/api/service?sleep=";
    public static final CloseableHttpClient httpClient = httpClient();

    private final SyncJiraApi fakeSyncJiraApi;
    private final String serviceUrl;

    public RealServerLatencySyncApi(SyncJiraApi fakeSyncJiraApi, String serverAddress) {
        this.fakeSyncJiraApi = fakeSyncJiraApi;
        this.serviceUrl = format(SERVICE_URL_TEMPLATE, serverAddress, PORT);
    }

    @Override
    public Set<JiraBundle> findBundlesByName(String bundleName) {
        String unusedButBlocksAWhile = blockingCallToTheRealServer(bundleName);
        out.println(unusedButBlocksAWhile); //avoid JIT optimization
        return fakeSyncJiraApi.findBundlesByName(bundleName);
    }

    @Override
    public Set<JiraComponent> findComponentsByBundle(JiraBundle bundle) {
        String unusedButBlocksAWhile = blockingCallToTheRealServer(bundle);
        out.println(unusedButBlocksAWhile); //avoid JIT optimization
        return fakeSyncJiraApi.findComponentsByBundle(bundle);
    }


    private static CloseableHttpClient httpClient() {
        return HttpClientBuilder.create()
                .setMaxConnPerRoute(RealServerLatencySyncApi.MAX_CONN)
                .setMaxConnTotal(RealServerLatencySyncApi.MAX_CONN)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(RealServerLatencySyncApi.TIMEOUT)
                        .setSocketTimeout(RealServerLatencySyncApi.TIMEOUT)
                        .setConnectionRequestTimeout(RealServerLatencySyncApi.TIMEOUT)
                        .build()
                )
                .build();
    }

    private String blockingCallToTheRealServer(Object param) {
        try {
            return httpClient.execute(new HttpGet(serviceUrl + sleepDuration(param)), basicResponseHandler);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected final Map<Object, Long> sleeps = new ConcurrentHashMap<>();

    protected long sleepDuration(Object request) {
        return sleeps.computeIfAbsent(
                request,
                k -> ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP)
        );
    }

    @Override public String description() {
        return getClass().getSimpleName();
    }

}
