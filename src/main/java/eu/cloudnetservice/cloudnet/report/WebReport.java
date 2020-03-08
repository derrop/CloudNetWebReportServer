package eu.cloudnetservice.cloudnet.report;

import java.util.Map;

public class WebReport {

    private String key;
    private int version;
    private long timestamp;
    private Map<String, Map<String, String>> replacements;

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getVersion() {
        return this.version;
    }

    public Map<String, Map<String, String>> getReplacements() {
        return this.replacements;
    }
}
