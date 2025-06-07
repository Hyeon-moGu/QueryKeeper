package com.querysentinel.engine;

import java.lang.reflect.Method;

public class TimeAssertionEngine {

    public static void assertExecutionTime(Method method, long duration, long expected) {
        if (duration > expected) {
            System.err.printf(
                "[QuerySentinel] ExpectTime FAILED - Method %s took %dms (expected <= %dms)\n",
                method.getName(), duration, expected
            );
            throw new AssertionError("Execution time exceeded: " + duration + "ms > " + expected + "ms");
        } else {
            System.out.printf(
                "[QuerySentinel] ExpectTime PASSED - Method %s took %dms (expected <= %dms)\n",
                method.getName(), duration, expected
            );
        }
    }
}
