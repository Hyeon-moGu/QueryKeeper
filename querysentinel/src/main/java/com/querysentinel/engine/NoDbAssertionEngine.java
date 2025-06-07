package com.querysentinel.engine;

import com.querysentinel.collector.QuerySentinelContext;

import java.lang.reflect.Method;

public class NoDbAssertionEngine {

    public static void assertNoDb(Method method) {
        long totalQueries = QuerySentinelContext.getLogs().size();

        if (totalQueries > 0) {
            System.err.printf(
                "[QuerySentinel] ExpectNoDb FAILED - %d DB queries were executed in %s()%n",
                totalQueries, method.getName()
            );
            throw new AssertionError("Expected no DB access, but found " + totalQueries + " queries.");
        } else {
            System.out.printf(
                "[QuerySentinel] ExpectNoDb PASSED - No DB access in %s()%n", method.getName()
            );
        }
    }
}
