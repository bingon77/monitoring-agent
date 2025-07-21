package com.example.agent;

import com.example.agent.Impl.DBConnectionFactory;
import com.example.agent.Impl.MonitorModule;
import com.example.agent.connectionfactory.OracleConnectionFactory;
import com.example.agent.connectionfactory.PostgresConnectionFactory;
import com.example.agent.module.ApacheMonitorModule;
import com.example.agent.module.DatabaseMonitorModule;
import com.example.agent.module.GeoServerMonitorModule;
import com.example.agent.module.OsMonitorModule;
import com.example.agent.util.AgentConfigVO;
import com.example.agent.util.AgentWebSocketClient;
import com.example.agent.util.HttpUtil;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class MonitoringAgent {
    private final List<MonitorModule> modules = new ArrayList<>();
    private final AgentConfigVO config;
    private final AgentWebSocketClient socket;



    public MonitoringAgent(AgentConfigVO config, AgentWebSocketClient socket) throws Exception {
        this.config = config;
        this.socket = socket;


        Set<String> enabledModules = Arrays.stream(config.getProperty("monitor.modules").split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        if (enabledModules.contains("os")) {
            modules.add(new OsMonitorModule());
        }

        // DB 연결팩토리 생성 및 DB 모듈 추가
        DBConnectionFactory dbFactory = null;
        if (enabledModules.contains("postgresql")) {
            dbFactory = new PostgresConnectionFactory(config.getRaw());
        } else if (enabledModules.contains("oracle")) {
            dbFactory = new OracleConnectionFactory(config.getRaw());
        }

        if (dbFactory != null) {
            Connection conn = dbFactory.createConnection();
            if (conn != null) {
                modules.add(new DatabaseMonitorModule(conn));
            }
        }

        if (enabledModules.contains("apache")) {
            String apacheUrl = config.getProperty("apache.status.url");
            modules.add(new ApacheMonitorModule(apacheUrl));
        }
        if (enabledModules.contains("geoserver")) {
            String geoUrl = config.getProperty("geoserver.api.url");
            modules.add(new GeoServerMonitorModule(geoUrl));
        }
    }

    public void collectAndSendData() {
        JSONObject fullData = new JSONObject();
        fullData.put("serverId", config.getServerId());

        for (MonitorModule module : modules) {
            try {
                JSONObject data = module.collectData();
                fullData.put(module.getName(), data);
            } catch (Exception e) {
                // 모듈별 예외 처리: 로그 출력 및 에러 메시지 JSON으로 추가
                JSONObject errorData = new JSONObject();
                errorData.put("error", e.getMessage());
                fullData.put(module.getName(), errorData);
                e.printStackTrace();
            }
        }


        System.out.println(fullData.toString(2));


        // TODO: 모니터링 서버로 HTTP POST 전송
        //HttpUtil.postJson(config.getProperty("monitoring.Server.Url"), fullData.toString());
        // TODO: 모니터링 서버로 WebSocket 전송
        try {
            if (socket.isOpen()) {
                socket.send(fullData.toString());
            } else {
                System.err.println("[WARN] WebSocket이 열려있지 않아 데이터 전송 실패");
            }
        } catch (Exception ex) {
            System.err.println("[ERROR] WebSocket 전송 실패: " + ex.getMessage());
        }

    }


}