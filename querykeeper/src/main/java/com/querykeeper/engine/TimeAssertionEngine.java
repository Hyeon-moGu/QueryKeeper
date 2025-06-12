package com.querykeeper.engine;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeAssertionEngine {

    private static final Logger logger = LoggerFactory.getLogger(TimeAssertionEngine.class);

    public static void assertExecutionTime(Method method, long duration, long expected) {
        String methodName = method.getName();

        if (expected == 0) {
            logger.warn("\n[QueryKeeper] ▶ ExpectTime (!) SKIPPED - {}() took {}ms (no threshold set)",
                    methodName, duration);
            return;
        }

        if (duration > expected) {
            logger.error("\n[QueryKeeper] ▶ ExpectTime X FAILED - {} took {}ms (expected <= {}ms)", methodName,
                    duration, expected);
            throw new AssertionError("Execution time exceeded: " + duration + "ms > " + expected + "ms");
        } else {
            logger.info("\n[QueryKeeper] ▶ ExpectTime ✓ PASSED - {} took {}ms (expected <= {}ms)", methodName,
                    duration, expected);
        }
    }
}
