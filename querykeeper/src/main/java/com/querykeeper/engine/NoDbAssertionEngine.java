package com.querykeeper.engine;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querykeeper.collector.QueryKeeperContext;

public class NoDbAssertionEngine {

    private static final Logger logger = LoggerFactory.getLogger(NoDbAssertionEngine.class);

    public static void assertNoDb(Method method) {
        long totalQueries = QueryKeeperContext.getCurrent().getLogs().size();
        String methodName = method.getName();

        if (totalQueries > 0) {
            logger.error("\n[QueryKeeper] ▶ ExpectNoDb X FAILED - {} DB queries were executed in {}()", totalQueries,
                    methodName);
            throw new AssertionError("\n[QueryKeeper] ▶ Expected no DB access, but found " + totalQueries + " queries");
        } else {
            logger.info("\n[QueryKeeper] ▶ ExpectNoDb ✓ PASSED - No DB access in {}()", methodName);
        }
    }
}
