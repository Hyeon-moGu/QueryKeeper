package com.querykeeper.collector;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DelegatingDataSource;

public class LoggingDataSource extends DelegatingDataSource {

    public LoggingDataSource(DataSource delegate) {
        super(delegate);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new LoggingConnection(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new LoggingConnection(super.getConnection(username, password));
    }
}
