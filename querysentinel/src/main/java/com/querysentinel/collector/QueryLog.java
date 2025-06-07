package com.querysentinel.collector;

import java.util.Map;

public class QueryLog {
    public final String sql;
    public final Map<Integer, Object> parameters;
    public final long durationMs;
    public final String caller;

    public QueryLog(String sql, Map<Integer, Object> parameters, long durationMs, String caller) {
        this.sql = sql;
        this.parameters = parameters;
        this.durationMs = durationMs;
        this.caller = caller;
    }
    

    public String getType() {
        String lower = sql.trim().toLowerCase();
        if (lower.startsWith("select")) return "SELECT";
        if (lower.startsWith("insert")) return "INSERT";
        if (lower.startsWith("update")) return "UPDATE";
        if (lower.startsWith("delete")) return "DELETE";
        return "OTHER";
    }
}
