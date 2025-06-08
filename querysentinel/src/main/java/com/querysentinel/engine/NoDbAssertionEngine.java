package com.querysentinel.engine;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querysentinel.collector.QuerySentinelContext;

public class NoDbAssertionEngine {

    private static final Logger logger = LoggerFactory.getLogger(NoDbAssertionEngine.class);

    public static void assertNoDb(Method method) {
        long totalQueries = QuerySentinelContext.getLogs().size();
        String methodName = method.getName();

        if (totalQueries > 0) {
            logger.error("\n[QuerySentinel] ExpectNoDb ❌ FAILED - {} DB queries were executed in {}()", totalQueries,
                    methodName);
            throw new AssertionError("Expected no DB access, but found " + totalQueries + " queries.");
        } else {
            logger.info("\n[QuerySentinel] ExpectNoDb ✅ PASSED - No DB access in {}()", methodName);
        }
    }
}
