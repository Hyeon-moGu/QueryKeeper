package com.querykeeper.collector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        if (lower.startsWith("select"))
            return "SELECT";
        if (lower.startsWith("insert"))
            return "INSERT";
        if (lower.startsWith("update"))
            return "UPDATE";
        if (lower.startsWith("delete"))
            return "DELETE";
        return "OTHER";
    }

    public String fingerprint() {
        if (isAutoGeneratedOrInternalQuery(sql))
            return null;

        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        parameters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append("|").append(e.getKey()).append("=").append(String.valueOf(e.getValue())));
        return sb.toString();
    }

    private static final List<Pattern> IGNORE_PATTERNS = Arrays.asList(
            Pattern.compile("^select\\s+next\\s+value\\s+for", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+currval", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+last_insert_id", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^call\\s+identity", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+@@identity", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+1(\\s+from.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+current_\\w+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+hibernate_sequence\\.nextval", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^pragma\\s+table_info", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^show\\s+create\\s+table", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+(nextval|currval)\\s*\\(?['\"]?\\w+['\"]?\\)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+.*from\\s+(information_schema|all_tab_columns|user_sequences)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+\\*\\s+from\\s+dual", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+sysdate\\s+from\\s+dual", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+user\\s+from\\s+dual", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+@@session\\.\\w+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+pg_catalog\\..*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+pg_get_serial_sequence.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+hibernate_sequence\\.nextval", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+sequence_name\\s+from\\s+all_sequences", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+constraint_name\\s+from\\s+user_constraints", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+\\*\\s+from\\s+information_schema\\.key_column_usage",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+1\\s+from\\s+dual\\s+where\\s+1\\s*=\\s*\\?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+version\\(\\)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+@@autocommit", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^select\\s+database\\(\\)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^call\\s+next\\s+value\\s+for", Pattern.CASE_INSENSITIVE));

    private static boolean isAutoGeneratedOrInternalQuery(String sql) {
        String normalized = sql.trim().toLowerCase().replaceAll("\\s+", " ");
        return IGNORE_PATTERNS.stream().anyMatch(p -> p.matcher(normalized).find());
    }
}
