package eu.cloudnetservice.cloudnet.report.web;

import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.network.http.IHttpContext;
import de.dytanic.cloudnet.driver.network.http.MethodHttpHandlerAdapter;
import eu.cloudnetservice.cloudnet.report.WebReport;
import eu.cloudnetservice.cloudnet.report.WebReportServer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CloudNetWebReportCreateHandler extends MethodHttpHandlerAdapter {

    private static final byte[] INVALID_REPORT = JsonDocument.newDocument().append("error", "Invalid report").toPrettyJson().getBytes(StandardCharsets.UTF_8);

    private WebReportServer reportServer;

    public CloudNetWebReportCreateHandler(WebReportServer reportServer) {
        this.reportServer = reportServer;
    }

    @Override
    public void handlePost(String path, IHttpContext context) throws Exception {
        WebReport webReport = JsonDocument.newDocument(context.request().bodyAsString()).toInstanceOf(WebReport.class);
        if (!this.validateReport(webReport)) {
            context.response()
                    .statusCode(403)
                    .body(INVALID_REPORT);
            return;
        }

        webReport.setTimestamp(System.currentTimeMillis());

        String key = this.reportServer.generateRandomKey();
        this.reportServer.putReport(key, webReport);

        context.response()
                .statusCode(200)
                .body(JsonDocument.newDocument().append("key", key).append("timeout", webReport.getTimestamp() + this.reportServer.getReportTimeoutMillis()).toPrettyJson());
    }

    private boolean validateReport(WebReport webReport) {
        if (webReport == null || webReport.getReplacements() == null) {
            return false;
        }
        for (Map.Entry<String, Map<String, String>> entry : webReport.getReplacements().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                return false;
            }

            for (Map.Entry<String, String> replacementEntry : entry.getValue().entrySet()) {
                if (replacementEntry.getKey() == null || replacementEntry.getValue() == null) {
                    return false;
                }
            }
        }

        return true;
    }

}
