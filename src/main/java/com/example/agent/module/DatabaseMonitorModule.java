package com.example.agent.module;

import com.example.agent.Impl.MonitorModule;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseMonitorModule implements MonitorModule {
    private Connection conn;

    public DatabaseMonitorModule(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String getName() {
        return "database";
    }

    @Override
    public JSONObject collectData() {
        JSONObject dbData = new JSONObject();

        try (Statement stmt = conn.createStatement()) {
            // 예시: PostgreSQL과 Oracle에 공통적으로 있는 세션 조회 쿼리
            // 필요시 DB 종류별 쿼리로 분기 처리 가능

            // PostgreSQL 예: SELECT count(*) FROM pg_stat_activity WHERE state='active'
            // Oracle 예: SELECT COUNT(*) FROM v$session WHERE status = 'ACTIVE'

            String dbProductName = conn.getMetaData().getDatabaseProductName().toLowerCase();

            String sql;
            if (dbProductName.contains("postgresql")) {
                sql = "SELECT count(*) as active_sessions FROM pg_stat_activity WHERE state='active'";
            } else if (dbProductName.contains("oracle")) {
                sql = "SELECT COUNT(*) as active_sessions FROM v$session WHERE status = 'ACTIVE'";
            } else {
                sql = ""; // 미지원 DB 처리
            }

            if (!sql.isEmpty()) {
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    dbData.put("activeSessions", rs.getInt("active_sessions"));
                }
            } else {
                dbData.put("error", "Unsupported DB type");
            }
        } catch (Exception e) {
            dbData.put("error", e.getMessage());
        }

        return dbData;
    }
}
