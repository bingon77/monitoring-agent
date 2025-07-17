package com.example.agent;

import com.example.agent.Impl.DBConnectionFactory;
import com.example.agent.Impl.MonitorModule;
import com.example.agent.connectionfactory.OracleConnectionFactory;
import com.example.agent.connectionfactory.PostgresConnectionFactory;
import com.example.agent.module.ApacheMonitorModule;
import com.example.agent.module.DatabaseMonitorModule;
import com.example.agent.module.GeoServerMonitorModule;
import com.example.agent.module.OsMonitorModule;
import com.example.agent.util.HttpUtil;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class MonitoringAgent {
    private static Properties config = new Properties();
    private List<MonitorModule> modules = new ArrayList<>();
    private String serverId;

    public MonitoringAgent() throws Exception {
        serverId = config.getProperty("server.id", "default-server");

        String modulesStr = config.getProperty("monitor.modules", "");
        Set<String> enabledModules = Arrays.stream(modulesStr.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        if (enabledModules.contains("os")) {
            modules.add(new OsMonitorModule());
        }

        // DB 연결팩토리 생성 및 DB 모듈 추가
        DBConnectionFactory dbFactory = null;
        if (enabledModules.contains("postgresql")) {
            dbFactory = new PostgresConnectionFactory(config);
        } else if (enabledModules.contains("oracle")) {
            dbFactory = new OracleConnectionFactory(config);
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
        fullData.put("serverId", serverId);

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

        // TODO: 모니터링 서버로 HTTP POST 전송 구현
        HttpUtil.postJson(config.getProperty("monitoring.Server.Url"), fullData.toString());
    }

    public static void main(String[] args) throws Exception {

        //properties 읽어오는 부분
        InputStream inputStream = MonitoringAgent.class.getClassLoader().getResourceAsStream("config.properties");
        config.load(inputStream);
        inputStream.close();
        
        MonitoringAgent agent = new MonitoringAgent();



        if (inputStream == null) {
            System.err.println("config.properties not found in classpath");
            return;
        }
        /*
        if (!HttpUtil.isServerAvailable(config.getProperty("monitoring.Server.Url"))) {
            System.err.println("모니터링 서버와 연결할 수 없습니다: " + config.getProperty("monitoring.Server.Url"));
            System.exit(1);  // 종료 또는 재시도 로직 추가 가능
            Thread.sleep(10000); // 10초 주기 실행
            main(args);

            return;
        }
        */




        //단일 테스트를 위하여 while 주석처리 상태
        agent.collectAndSendData();
//        while (true) {
//            agent.collectAndSendData();
//            Thread.sleep(10000); // 10초 주기 실행
//        }
    }
}