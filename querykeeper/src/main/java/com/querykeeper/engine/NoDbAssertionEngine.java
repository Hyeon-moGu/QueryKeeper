package com.querykeeper.engine;

import java.lang.reflect.Method;
import java.util.List;

import com.querykeeper.collector.QueryKeeperContext;

public class NoDbAssertionEngine {

    public static void assertNoDb(Method method, List<String> finalLog, List<Throwable> finalFailures) {
        long totalQueries = QueryKeeperContext.getCurrent().getLogs().size();
        String methodName = method.getName();

        if (totalQueries > 0) {
            finalLog.add("[QueryKeeper] ▶ ExpectNoDb X FAILED - " + totalQueries + " DB queries were executed in "
                    + methodName + "()");
            finalFailures.add(new AssertionError(
                    "[QueryKeeper] ▶ Expected no DB access, but found " + totalQueries + " queries"));
        } else {
            finalLog.add("[QueryKeeper] ▶ ExpectNoDb ✓ PASSED - No DB access in " + methodName + "()");
        }
    }
}
