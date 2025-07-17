package com.example.agent.module;

import com.example.agent.Impl.MonitorModule;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

public class ApacheMonitorModule implements MonitorModule {
    private String statusUrl;

    public ApacheMonitorModule(String statusUrl) {
        this.statusUrl = statusUrl;
    }

    @Override
    public String getName() {
        return "apache";
    }

    @Override
    public JSONObject collectData() {
        JSONObject data = new JSONObject();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(statusUrl);
            try (CloseableHttpResponse response = client.execute(get)) {
                String body = EntityUtils.toString(response.getEntity());
                // 간단히 Total Accesses 값 파싱 예시
                String[] lines = body.split("\n");
                for (String line : lines) {
                    if (line.startsWith("Total Accesses:")) {
                        data.put("totalAccesses", Integer.parseInt(line.split(":")[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            data.put("error", e.getMessage());
        }
        return data;
    }
}
