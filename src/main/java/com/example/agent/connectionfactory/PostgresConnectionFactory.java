package com.example.agent.connectionfactory;

import com.example.agent.Impl.DBConnectionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresConnectionFactory implements DBConnectionFactory {
    private String url;
    private String user;
    private String password;

    public PostgresConnectionFactory(Properties props) {
        this.url = props.getProperty("postgresql.url");
        this.user = props.getProperty("postgresql.user");
        this.password = props.getProperty("postgresql.password");
    }

    @Override
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}