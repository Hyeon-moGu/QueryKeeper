package com.querysentinel.collector;

import java.util.ArrayList;
import java.util.List;

public class QuerySentinelContext {
    private static final ThreadLocal<List<QueryLog>> logs = ThreadLocal.withInitial(ArrayList::new);

    public static void clear() {
        logs.get().clear();
    }

    public static void log(QueryLog log) {
        logs.get().add(log);
    }

    public static List<QueryLog> getLogs() {
        return logs.get();
    }
}