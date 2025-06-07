package com.querysentinel.collector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoggingConnection extends AbstractDelegatingConnection {

    public LoggingConnection(Connection delegate) {
        super(delegate);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new LoggingPreparedStatement(super.prepareStatement(sql), sql);
    }

}
