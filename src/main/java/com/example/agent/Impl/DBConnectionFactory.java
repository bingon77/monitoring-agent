package com.example.agent.Impl;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBConnectionFactory {
    Connection createConnection() throws SQLException;
}
