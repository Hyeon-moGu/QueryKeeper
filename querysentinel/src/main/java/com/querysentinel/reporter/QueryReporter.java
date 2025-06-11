package com.querysentinel.reporter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querysentinel.collector.QueryLog;
import com.querysentinel.collector.QuerySentinelContext;

public class QueryReporter {
    private static final Logger logger = LoggerFactory.getLogger(QueryReporter.class);

    public static void printReport(String testName, boolean skipped) {
        List<QueryLog> logs = QuerySentinelContext.getCurrent().getLogs();
        StringBuilder sb = new StringBuilder();
        int num = 1;

        if (skipped) {
            sb.append("[QuerySentinel] ExpectQuery ⚠️ SKIPPED - ").append(testName)
                    .append(", No expectations set, logging queries only.").append("\n");
        } else {
            sb.append("[QuerySentinel] ExpectQuery ✅ PASSED - ").append(testName).append("\n");
        }

        sb.append("--------------------------------------------------------\n");
        sb.append("Total Queries: ").append(logs.size()).append("\n");
        sb.append("--------------------------------------------------------\n");

        for (QueryLog log : logs) {
            sb.append(num).append(".").append(" [").append(log.getType()).append("] (").append(log.durationMs)
                    .append(" ms)\n");
            sb.append("SQL     : ").append(log.sql.replaceAll("\n", " ")).append("\n");
            if (!log.parameters.isEmpty()) {
                sb.append("Params  : ").append(log.parameters).append("\n");
            }
            sb.append("Caller  : ").append(log.caller).append("\n");
            sb.append("--------------------------------------------------------\n");
            num++;
        }

        logger.info("\n{}\n", sb);
    }

}