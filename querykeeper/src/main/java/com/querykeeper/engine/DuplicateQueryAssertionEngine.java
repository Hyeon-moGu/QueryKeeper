package com.querykeeper.engine;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.querykeeper.collector.QueryKeeperContext;
import com.querykeeper.collector.QueryLog;

public class DuplicateQueryAssertionEngine {

    public static void assertNoExcessiveDuplicates(Method method, int maxAllowed, List<String> finalLog,
            List<Throwable> finalFailures) {
        List<QueryLog> logs = QueryKeeperContext.getCurrent().getLogs();

        Map<String, Integer> fingerprintCount = new HashMap<>();

        for (QueryLog log : logs) {
            String fp = log.fingerprint();
            if (fp != null) {
                fingerprintCount.merge(fp, 1, Integer::sum);
            }
        }

        int totalDuplicates = fingerprintCount.values().stream()
                .mapToInt(count -> count - 1)
                .filter(v -> v > 0)
                .sum();

        if (totalDuplicates > maxAllowed) {
            finalLog.add("[QueryKeeper] ▶ ExpectDuplicateQuery X FAILED - Found "
                    + totalDuplicates + " duplicate queries (allowed: " + maxAllowed + ")");
            fingerprintCount.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .forEach(entry -> {
                        finalLog.add("  • Duplicate [" + entry.getValue() + "x] → " + entry.getKey());
                    });

            finalFailures.add(new AssertionError(
                    "[QueryKeeper] ▶ Expected at most " + maxAllowed + " duplicate queries, but found "
                            + totalDuplicates));
        } else {
            finalLog.add("[QueryKeeper] ▶ ExpectDuplicateQuery ✓ PASSED - "
                    + totalDuplicates + " duplicate queries (allowed: " + maxAllowed + ")");
        }
    }
}
