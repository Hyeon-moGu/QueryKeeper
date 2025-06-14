package com.querykeeper.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QueryKeeperContext {

    private static final ThreadLocal<QueryKeeperContext> CURRENT = ThreadLocal.withInitial(QueryKeeperContext::new);
    private static final ThreadLocal<List<QueryLog>> QUERY_LOGS = ThreadLocal.withInitial(ArrayList::new);
    private final Map<String, Integer> queryFingerprintCount = new HashMap<>();

    private final Map<String, DetachedAccessInfo> detachedAccessMap = new ConcurrentHashMap<>();

    public static QueryKeeperContext getCurrent() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.set(new QueryKeeperContext());
        QUERY_LOGS.set(new ArrayList<>());
    }

    public void log(QueryLog log) {
        QUERY_LOGS.get().add(log);

        String fp = log.fingerprint();
        queryFingerprintCount.merge(fp, 1, Integer::sum);
    }

    public List<QueryLog> getLogs() {
        return QUERY_LOGS.get();
    }

    public Map<String, Integer> getDuplicateQueryFingerprints() {
        return queryFingerprintCount.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void markDetachedAccess(String entityName, String fieldName, String rootEntityName, String fullPath) {
        detachedAccessMap.put(entityName, new DetachedAccessInfo(entityName, fieldName, rootEntityName, fullPath));
    }

    public DetachedAccessInfo getDetachedAccessInfo(String entityName) {
        return detachedAccessMap.get(entityName);
    }

    public boolean hadDetachedAccess(String entityName) {
        return detachedAccessMap.containsKey(entityName);
    }

    public Set<String> getEntitiesWithDetachedAccess() {
        return detachedAccessMap.keySet();
    }

    public static class DetachedAccessInfo {
        public final String entity;
        public final String field;
        public final String rootEntity;
        public final String fullPath;

        public DetachedAccessInfo(String entity, String field, String rootEntity, String fullPath) {
            this.entity = entity;
            this.field = field;
            this.rootEntity = rootEntity;
            this.fullPath = fullPath;
        }
    }
}
