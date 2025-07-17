package com.example.agent.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpUtil {
    public static void postJson(String url, String json) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json, "UTF-8"));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Response Code : " + statusCode);
                // 필요하면 응답 본문 처리
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 서버 접속 가능 여부 확인
    public static boolean isServerAvailable(String url) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                return (statusCode >= 200 && statusCode < 300); // 2xx 성공 범위면 OK
            }
        } catch (Exception e) {
            return false;
        }
    }
}