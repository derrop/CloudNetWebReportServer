package eu.cloudnetservice.cloudnet.report;

import de.dytanic.cloudnet.common.StringUtil;
import de.dytanic.cloudnet.driver.network.http.IHttpServer;
import de.dytanic.cloudnet.driver.network.netty.NettyHttpServer;
import eu.cloudnetservice.cloudnet.report.web.CloudNetWebReportAssetsHandler;
import eu.cloudnetservice.cloudnet.report.web.CloudNetWebReportCreateHandler;
import eu.cloudnetservice.cloudnet.report.web.CloudNetWebReportHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebReportServer {

    private final int keyLength = Math.max(3, Integer.getInteger("cloudnet.report.key.length", 30));
    private final long reportTimeoutMillis = Long.getLong("cloudnet.report.timeout", 600_000);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private IHttpServer httpServer;

    private Map<String, WebReport> activeReports = new ConcurrentHashMap<>();

    private WebReportServer() throws Exception {
        this.httpServer = new NettyHttpServer();
    }

    public void start() {
        this.httpServer.redirect("/{key}", "/{key}/index.html");
        this.httpServer.get("/{key}/{type}", new CloudNetWebReportHandler(this));
        CloudNetWebReportAssetsHandler assetsHandler = new CloudNetWebReportAssetsHandler();
        this.httpServer.get(assetsHandler.getPath(), assetsHandler);

        this.httpServer.rateLimit("/", TimeUnit.HOURS, 20);
        this.httpServer.post("/", new CloudNetWebReportCreateHandler(this));

        this.httpServer.addListener(Integer.getInteger("cloudnet.report.web.port", 1430));

        this.executorService.execute(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(this.activeReports.isEmpty() ? this.reportTimeoutMillis : 30000);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                for (WebReport value : this.activeReports.values()) {
                    if (value.getTimestamp() + this.reportTimeoutMillis >= System.currentTimeMillis()) {
                        this.activeReports.remove(value.getKey());
                    }
                }
            }
        });
    }

    public String generateRandomKey() {
        String key;
        do {
            key = StringUtil.generateRandomString(this.keyLength);
        } while (this.activeReports.containsKey(key));
        return key;
    }

    public WebReport getReport(String key) {
        return this.activeReports.get(key);
    }

    public void putReport(String key, WebReport webReport) {
        webReport.setKey(key);
        this.activeReports.put(key, webReport);
    }

    public long getReportTimeoutMillis() {
        return this.reportTimeoutMillis;
    }

    public static void main(String[] args) throws Exception {
        WebReportServer webReportServer = new WebReportServer();
        webReportServer.start();
    }

}
