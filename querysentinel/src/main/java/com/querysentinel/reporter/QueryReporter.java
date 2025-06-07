package com.querysentinel.reporter;

import java.util.List;

import com.querysentinel.collector.QueryLog;
import com.querysentinel.collector.QuerySentinelContext;

public class QueryReporter {
    public static void printReport(String testName) {
        List<QueryLog> logs = QuerySentinelContext.getLogs();
        System.out.println("\n[QuerySentinel] Query Expectation PASSED - " + testName);
        System.out.println("--------------------------------------------------------");

        for (QueryLog log : logs) {
            System.out.printf("[%s] (%d ms)\n", log.getType(), log.durationMs);
            System.out.println("SQL     : " + log.sql.replaceAll("\n", " "));
            if(log.parameters.size()>0){
                System.out.println("Params  : " + log.parameters);
            }
            System.out.println("Caller  : " + log.caller);
            System.out.println("--------------------------------------------------------");
        }

        System.out.println(" Total Queries: " + logs.size());
    }
}