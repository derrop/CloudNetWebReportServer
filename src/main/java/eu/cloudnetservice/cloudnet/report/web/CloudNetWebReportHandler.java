package eu.cloudnetservice.cloudnet.report.web;

import de.dytanic.cloudnet.common.io.FileUtils;
import de.dytanic.cloudnet.driver.network.http.IHttpContext;
import de.dytanic.cloudnet.driver.network.http.IHttpHandler;
import eu.cloudnetservice.cloudnet.report.WebReport;
import eu.cloudnetservice.cloudnet.report.WebReportServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CloudNetWebReportHandler implements IHttpHandler {

    private WebReportServer reportServer;

    public CloudNetWebReportHandler(WebReportServer reportServer) {
        this.reportServer = reportServer;
    }

    @Override
    public void handle(String path, IHttpContext context) throws Exception {
        String key = context.request().pathParameters().get("key");
        String type = context.request().pathParameters().get("type");
        if (key == null || key.isEmpty()) {
            return;
        }
        if (type == null || type.isEmpty()) {
            type = "index.html";
        }
        if (type.contains("/")) {
            return;
        }
        WebReport webReport = this.reportServer.getReport(key);
        if (webReport == null) {
            return;
        }
        Map<String, String> replacements = webReport.getReplacements().get(type);

        if (replacements == null) {
            return;
        }

        String response = this.loadFile(type);
        if (response == null) {
            return;
        }

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String value = entry.getValue() == null || entry.getValue().isEmpty() ?
                    "not available" :
                    entry.getValue();
            response = response.replace("${" + entry.getKey() + "}", value);
        }

        context.response()
                .header("Content-Type", "text/html")
                .statusCode(200)
                .body(response)
                .context();
    }

    private String loadFile(String path) {
        try (InputStream inputStream = CloudNetWebReportHandler.class.getClassLoader().getResourceAsStream("web/" + path)) {
            if (inputStream == null) {
                return null;
            }

            return new String(FileUtils.toByteArray(inputStream), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
