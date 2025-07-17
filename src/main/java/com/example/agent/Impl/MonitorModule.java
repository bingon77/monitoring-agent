package com.example.agent.Impl;

import org.json.JSONObject;

public interface MonitorModule {
    String getName();

    /**
     * 데이터를 수집하는 메서드.
     * 예외가 발생할 수 있으므로 throws 선언 포함.
     */
    JSONObject collectData() throws Exception;
}