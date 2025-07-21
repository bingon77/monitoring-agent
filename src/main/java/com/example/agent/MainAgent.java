package com.example.agent;

import com.example.agent.util.AgentConfigVO;
import com.example.agent.util.AgentWebSocketClient;

import java.io.InputStream;
import java.util.Properties;

public class MainAgent {
    public static void main(String[] args) throws Exception {

        // 설정 로딩
        InputStream inputStream = MonitoringAgent.class.getClassLoader().getResourceAsStream("config.properties");
        if (inputStream == null) {
            System.err.println("config.properties not found in classpath");
            return;
        }

        Properties rawProps = new Properties();
        rawProps.load(inputStream);
        inputStream.close();

        AgentConfigVO config = new AgentConfigVO(rawProps);
        AgentWebSocketClient socket = new AgentWebSocketClient(config.getWebSocketUrl(), config.getMaxReconnectAttempts());

        socket.connectBlocking();  // 최초 연결
        socket.start24HourMonitor();  // 24시간 모니터 시작

        MonitoringAgent agent = new MonitoringAgent(config, socket);
        agent.collectAndSendData();  // 단일 테스트용

//        while (true) {
//            agent.collectAndSendData();
//            Thread.sleep(10000); // 10초마다 수집 전송
//        }


    }
}
