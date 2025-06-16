package com.querykeeper.engine;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.querykeeper.annotation.ExpectQuery;
import com.querykeeper.collector.QueryKeeperContext;
import com.querykeeper.collector.QueryLog;

public class QueryAssertionEngine {

    public static boolean assertQueries(Method method, ExpectQuery expectation,
            List<String> finalLog,
            List<Throwable> finalFailures) {
        String methodName = method.getName();
        List<QueryLog> logs = QueryKeeperContext.getCurrent().getLogs();
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
            finalLog.add("[QueryKeeper] ▶ ExpectQuery (!) SKIPPED - " + methodName +
                    ", No expectations set, logging queries only.");
            appendQueryList(finalLog, logs);
            return false;
        }

        boolean fail = (expectation.select() >= 0 && select != expectation.select()) ||
                (expectation.insert() >= 0 && insert != expectation.insert()) ||
                (expectation.update() >= 0 && update != expectation.update()) ||
                (expectation.delete() >= 0 && delete != expectation.delete());

        if (fail) {
            sb.append("[QueryKeeper] ▶ ExpectQuery X FAILED\n");
            sb.append("--------------------------------------------------------\n");

            sb.append("Expected - (");
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
            sb.append("), ");

            sb.append("Actual - (");
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
            sb.append(")");

            finalLog.add(sb.toString());
            appendQueryList(finalLog, logs);

            if (expectation.select() >= 0 && select != expectation.select()) {
                finalFailures.add(new AssertionError(
                        "[QueryKeeper] ▶ SELECT mismatch: expected=" + expectation.select() + ", actual=" + select));
            }
            if (expectation.insert() >= 0 && insert != expectation.insert()) {
                finalFailures.add(new AssertionError(
                        "[QueryKeeper] ▶ INSERT mismatch: expected=" + expectation.insert() + ", actual=" + insert));
            }
            if (expectation.update() >= 0 && update != expectation.update()) {
                finalFailures.add(new AssertionError(
                        "[QueryKeeper] ▶ UPDATE mismatch: expected=" + expectation.update() + ", actual=" + update));
            }
            if (expectation.delete() >= 0 && delete != expectation.delete()) {
                finalFailures.add(new AssertionError(
                        "[QueryKeeper] ▶ DELETE mismatch: expected=" + expectation.delete() + ", actual=" + delete));
            }

        } else {
            finalLog.add("[QueryKeeper] ▶ ExpectQuery ✓ PASSED - " + methodName);
            appendQueryList(finalLog, logs);
        }

        return true;
    }

    private static void appendQueryList(List<String> log, List<QueryLog> logs) {
        log.add("--------------------------------------------------------");
        log.add("Total Queries: " + logs.size());
        log.add("--------------------------------------------------------");

        int num = 1;
        for (QueryLog q : logs) {
            log.add(num + ". [" + q.getType() + "] (" + q.durationMs + " ms)");

            String formattedSql = q.sql;
            if (!q.parameters.isEmpty()) {
                formattedSql = formatSqlWithParams(q.sql, q.parameters);
            }

            log.add("SQL     : " + formattedSql);
            log.add("Caller  : " + q.caller);
            log.add("--------------------------------------------------------");
            num++;
        }
    }

    private static String formatSqlWithParams(String sql, Map<Integer, Object> params) {
        if (!sql.contains("?"))
            return sql;
        StringBuilder result = new StringBuilder();
        int paramIndex = 1;
        int lastIndex = 0;

        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                result.append(sql, lastIndex, i);

                Object value = params.get(paramIndex++);
                String replacement = (value instanceof Number) ? value.toString() : "'" + value + "'";
                result.append(replacement);

                lastIndex = i + 1;
            }
        }
        result.append(sql.substring(lastIndex));
        return result.toString();
    }
}
