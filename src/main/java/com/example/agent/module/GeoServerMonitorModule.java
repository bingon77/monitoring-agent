package com.example.agent.module;

import com.example.agent.Impl.MonitorModule;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

public class GeoServerMonitorModule implements MonitorModule {
    private String apiUrl;

    public GeoServerMonitorModule(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public String getName() {
        return "geoserver";
    }

    @Override
    public JSONObject collectData() {
        JSONObject data = new JSONObject();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(apiUrl);
            try (CloseableHttpResponse response = client.execute(get)) {
                String body = EntityUtils.toString(response.getEntity());
                // 필요에 따라 JSON 파싱 추가
                data.put("rawResponse", body);
            }
        } catch (IOException e) {
            data.put("error", e.getMessage());
        }
        return data;
    }
}
