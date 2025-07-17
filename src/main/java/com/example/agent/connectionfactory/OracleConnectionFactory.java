package com.example.agent.connectionfactory;

import com.example.agent.Impl.DBConnectionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class OracleConnectionFactory implements DBConnectionFactory {
    private String url;
    private String user;
    private String password;

    public OracleConnectionFactory(Properties props) {
        this.url = props.getProperty("oracle.url");
        this.user = props.getProperty("oracle.user");
        this.password = props.getProperty("oracle.password");
    }

    @Override
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
