package com.querysentinel.engine;

import com.querysentinel.annotation.ExpectQuery;
import com.querysentinel.collector.QueryLog;
import com.querysentinel.collector.QuerySentinelContext;

import java.util.List;

public class QueryAssertionEngine {

    public static void assertQueries(ExpectQuery expectation) {
        List<QueryLog> logs = QuerySentinelContext.getLogs();

        long select = logs.stream().filter(l -> l.getType().equals("SELECT")).count();
        long insert = logs.stream().filter(l -> l.getType().equals("INSERT")).count();
        long update = logs.stream().filter(l -> l.getType().equals("UPDATE")).count();
        long delete = logs.stream().filter(l -> l.getType().equals("DELETE")).count();

        StringBuilder debugLog = new StringBuilder();
        debugLog.append("\n[QuerySentinel] Query Expectation Failed\n");
        debugLog.append("--------------------------------------------------------\n");

        debugLog.append("Expected - ");
        if (expectation.select() >= 0) debugLog.append("SELECT: ").append(expectation.select()).append(", ");
        if (expectation.insert() >= 0) debugLog.append("INSERT: ").append(expectation.insert()).append(", ");
        if (expectation.update() >= 0) debugLog.append("UPDATE: ").append(expectation.update()).append(", ");
        if (expectation.delete() >= 0) debugLog.append("DELETE: ").append(expectation.delete()).append(", ");
        if (debugLog.charAt(debugLog.length() - 2) == ',') debugLog.setLength(debugLog.length() - 2);
        debugLog.append("\n");

        debugLog.append("Actual   - ");
        if (expectation.select() >= 0) debugLog.append("SELECT: ").append(select).append(", ");
        if (expectation.insert() >= 0) debugLog.append("INSERT: ").append(insert).append(", ");
        if (expectation.update() >= 0) debugLog.append("UPDATE: ").append(update).append(", ");
        if (expectation.delete() >= 0) debugLog.append("DELETE: ").append(delete).append(", ");
        if (debugLog.charAt(debugLog.length() - 2) == ',') debugLog.setLength(debugLog.length() - 2);
        debugLog.append("\n");

        debugLog.append("--------------------------------------------------------\n");
        for (QueryLog log : logs) {
            debugLog.append(String.format("[%s] (%d ms)\n", log.getType(), log.durationMs));
            debugLog.append("SQL     : ").append(log.sql).append("\n");
            if(log.parameters.size()>0){
                debugLog.append("Params  : ").append(log.parameters).append("\n");
            }
            debugLog.append("Caller  : ").append(log.caller).append("\n");
            debugLog.append("--------------------------------------------------------\n");
        }

        if (expectation.select() >= 0 && select != expectation.select()) {
            System.err.println(debugLog);
            throw new AssertionError("SELECT mismatch: expected=" + expectation.select() + ", actual=" + select);
        }
        if (expectation.insert() >= 0 && insert != expectation.insert()) {
            System.err.println(debugLog);
            throw new AssertionError("INSERT mismatch: expected=" + expectation.insert() + ", actual=" + insert);
        }
        if (expectation.update() >= 0 && update != expectation.update()) {
            System.err.println(debugLog);
            throw new AssertionError("UPDATE mismatch: expected=" + expectation.update() + ", actual=" + update);
        }
        if (expectation.delete() >= 0 && delete != expectation.delete()) {
            System.err.println(debugLog);
            throw new AssertionError("DELETE mismatch: expected=" + expectation.delete() + ", actual=" + delete);
        }
    }
}
