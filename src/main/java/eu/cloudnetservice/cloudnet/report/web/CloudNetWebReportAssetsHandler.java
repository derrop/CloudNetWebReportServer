package eu.cloudnetservice.cloudnet.report.web;

import de.dytanic.cloudnet.common.io.FileUtils;
import de.dytanic.cloudnet.driver.network.http.IHttpContext;
import de.dytanic.cloudnet.driver.network.http.IHttpHandler;

import java.io.InputStream;

public class CloudNetWebReportAssetsHandler implements IHttpHandler {

    private final String path = "/{key}/assets/*";
    private final String replacePath = "//assets/";

    public String getPath() {
        return this.path;
    }

    @Override
    public void handle(String path, IHttpContext context) throws Exception {
        if (path.contains("..")) {
            return;
        }

        path = path.substring(this.replacePath.length() + context.request().pathParameters().get("key").length());

        try (InputStream inputStream = CloudNetWebReportAssetsHandler.class.getClassLoader().getResourceAsStream("web/assets/" + path)) {
            if (inputStream != null) {
                context.response()
                        .body(FileUtils.toByteArray(inputStream))
                        .header("Content-Type", path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "application/js" : "text/plain")
                        .statusCode(200);
            }
        }

    }

}
