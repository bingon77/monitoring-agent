package com.example.agent.util;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class AgentWebSocketClient extends WebSocketClient {

    private long connectedTime;
    private int reconnectAttempts = 0;
    private final long MAX_CONNECTION_DURATION = 24 * 60 * 60 * 1000L; // 24시간
    private final int maxReconnectAttempts;


    public AgentWebSocketClient(String uri, int maxReconnectAttempts) throws URISyntaxException {
        super(new URI(uri));
        this.maxReconnectAttempts = maxReconnectAttempts;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("WebSocket 연결됨.");
        //연결 성공 시 연결 시간과 횟수 초기화
        connectedTime = System.currentTimeMillis();
        reconnectAttempts = 0;
    }

    @Override
    public void onMessage(String message) {
        System.out.println("서버로부터 응답: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket 연결 종료됨: " + reason+", "+"원격지 : "+remote);
        attemptReconnect();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket 오류 발생: " + ex.getMessage());
        attemptReconnect();
    }


    /**
     * WebSocket의 연결이나 연결의 안정성을 위하여
     * 24마다 연결을 체크 또는 재연결하여 확인 하도록
     * 검증 또는 재연결 하는 방식으로 구현하였습니다.
     */
    public void start24HourMonitor() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                long now = System.currentTimeMillis();
                if (now - connectedTime >= MAX_CONNECTION_DURATION) {
                    System.out.println("[INFO] 24시간 경과. 재연결 수행.");
                    attemptReconnect();
                }
            }
        }, 0, 60 * 1000); // 1분마다 체크
    }

    /**
     * WebSocket의 연결이나 연결의 안정성을 위하여
     * 최대 재연결 횟수(`maxReconnectAttempts`)를 초과하지 않는 범위에서
     * 일정 시간 간격(3초)으로 재연결을 반복합니다.
     *
     * 최초 재귀 함수를 사용하였으나, JVM의 오버풀 현상에 대하여 검색 하다 반복문으로 변경
     * 반복문을 이용하여 재연결 시도합니다.
     */
    private void attemptReconnect() {
        while (reconnectAttempts < maxReconnectAttempts) {
            try {
                reconnectAttempts++;
                System.out.println("[RECONNECT] 재연결 시도: " + reconnectAttempts);
                Thread.sleep(3000);
                this.reconnectBlocking();
                return; // 성공하면 종료
            } catch (Exception e) {
                System.err.println("[FAIL] 재연결 실패: " + e.getMessage());
            }
        }
        System.err.println("[STOP] 최대 재연결 횟수 초과. 종료합니다.");
    }
}
