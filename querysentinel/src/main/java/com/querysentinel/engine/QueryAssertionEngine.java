package com.querysentinel.engine;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querysentinel.annotation.ExpectQuery;
import com.querysentinel.collector.QueryLog;
import com.querysentinel.collector.QuerySentinelContext;
import com.querysentinel.reporter.QueryReporter;

public class QueryAssertionEngine {

    private static final Logger logger = LoggerFactory.getLogger(QueryAssertionEngine.class);

    public static boolean assertQueries(Method method, ExpectQuery expectation) {
        String methodName = method.getName();
        List<QueryLog> logs = QuerySentinelContext.getLogs();
        StringBuilder sb = new StringBuilder();

        long select = logs.stream().filter(l -> l.getType().equals("SELECT")).count();
        long insert = logs.stream().filter(l -> l.getType().equals("INSERT")).count();
        long update = logs.stream().filter(l -> l.getType().equals("UPDATE")).count();
        long delete = logs.stream().filter(l -> l.getType().equals("DELETE")).count();

        boolean hasExpectation = expectation.select() >= 0 ||
                expectation.insert() >= 0 ||
                expectation.update() >= 0 ||
                expectation.delete() >= 0;

        if (!hasExpectation) {
            QueryReporter.printReport(methodName, true);
            return false;
        }

        sb.append("\n[QuerySentinel] ExpectQuery ❌ FAILED\n");
        sb.append("--------------------------------------------------------\n");

        sb.append("Expected - ");
        if (expectation.select() >= 0)
            sb.append("SELECT: ").append(expectation.select()).append(", ");
        if (expectation.insert() >= 0)
            sb.append("INSERT: ").append(expectation.insert()).append(", ");
        if (expectation.update() >= 0)
            sb.append("UPDATE: ").append(expectation.update()).append(", ");
        if (expectation.delete() >= 0)
            sb.append("DELETE: ").append(expectation.delete()).append(", ");
        if (sb.charAt(sb.length() - 2) == ',')
            sb.setLength(sb.length() - 2);
        sb.append("\n");

        sb.append("Actual   - ");
        if (expectation.select() >= 0)
            sb.append("SELECT: ").append(select).append(", ");
        if (expectation.insert() >= 0)
            sb.append("INSERT: ").append(insert).append(", ");
        if (expectation.update() >= 0)
            sb.append("UPDATE: ").append(update).append(", ");
        if (expectation.delete() >= 0)
            sb.append("DELETE: ").append(delete).append(", ");
        if (sb.charAt(sb.length() - 2) == ',')
            sb.setLength(sb.length() - 2);
        sb.append("\n");

        sb.append("--------------------------------------------------------\n");
        sb.append(" Total Queries: ").append(logs.size()).append("\n");
        sb.append("--------------------------------------------------------\n");

        int num = 1;
        for (QueryLog log : logs) {
            sb.append(num).append(". ").append("[").append(log.getType()).append("] (")
                    .append(log.durationMs).append(" ms)\n");
            sb.append("SQL     : ").append(log.sql.replaceAll("\n", " ")).append("\n");
            if (!log.parameters.isEmpty()) {
                sb.append("Params  : ").append(log.parameters).append("\n");
            }
            sb.append("Caller  : ").append(log.caller).append("\n");
            sb.append("--------------------------------------------------------\n");
            num++;
        }

        if ((expectation.select() >= 0 && select != expectation.select()) ||
                (expectation.insert() >= 0 && insert != expectation.insert()) ||
                (expectation.update() >= 0 && update != expectation.update()) ||
                (expectation.delete() >= 0 && delete != expectation.delete())) {

            logger.error(sb.toString());

            if (expectation.select() >= 0 && select != expectation.select()) {
                throw new AssertionError("SELECT mismatch: expected=" + expectation.select() + ", actual=" + select);
            }
            if (expectation.insert() >= 0 && insert != expectation.insert()) {
                throw new AssertionError("INSERT mismatch: expected=" + expectation.insert() + ", actual=" + insert);
            }
            if (expectation.update() >= 0 && update != expectation.update()) {
                throw new AssertionError("UPDATE mismatch: expected=" + expectation.update() + ", actual=" + update);
            }
            if (expectation.delete() >= 0 && delete != expectation.delete()) {
                throw new AssertionError("DELETE mismatch: expected=" + expectation.delete() + ", actual=" + delete);
            }
        }
        QueryReporter.printReport(methodName, false);
        return true;
    }
}
