package com.querykeeper.collector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractDelegatingPreparedStatement implements PreparedStatement {
    protected final PreparedStatement delegate;

    public AbstractDelegatingPreparedStatement(PreparedStatement delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    public void addBatch() throws SQLException {
        delegate.addBatch();
    }
}
