package com.querysentinel.collector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class QuerySentinelContext {

    private static final ThreadLocal<QuerySentinelContext> CURRENT = ThreadLocal.withInitial(QuerySentinelContext::new);
    private static final ThreadLocal<List<QueryLog>> logs = ThreadLocal.withInitial(ArrayList::new);

    private final Map<String, Integer> lazyLoadCounts = new ConcurrentHashMap<>();
    private final Map<String, Boolean> lazyLoadExceptions = new ConcurrentHashMap<>();

    public void markLazyLoadException(String entityName) {
        lazyLoadExceptions.put(entityName, true);
    }

    public boolean hadLazyLoadException(String entityName) {
        return lazyLoadExceptions.getOrDefault(entityName, false);
    }

    public static QuerySentinelContext getCurrent() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.set(new QuerySentinelContext());
        logs.set(new ArrayList<>());
    }

    public void log(QueryLog log) {
        logs.get().add(log);
    }

    public List<QueryLog> getLogs() {
        return logs.get();
    }

    public void registerLazyLoad(String entityName) {
        lazyLoadCounts.merge(entityName, 1, Integer::sum);
    }

    public int getLazyLoadCount(String entityName) {
        return lazyLoadCounts.getOrDefault(entityName, 0);
    }

    public Set<String> getAllEntitiesWithLazyLoad() {
        Set<String> names = new HashSet<>(lazyLoadCounts.keySet());
        names.addAll(lazyLoadExceptions.keySet());
        return names;
    }
}