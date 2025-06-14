package com.querykeeper.engine;

import java.lang.reflect.Method;
import java.util.List;

public class TimeAssertionEngine {

    public static void assertExecutionTime(
            Method method,
            long duration,
            long expected,
            List<String> finalLog,
            List<Throwable> finalFailures) {

        String methodName = method.getName();

        if (expected == 0) {
            finalLog.add("[QueryKeeper] ▶ ExpectTime (!) SKIPPED - " + methodName +
                    "() took " + duration + "ms (no threshold set)");
            return;
        }

        if (duration > expected) {
            String msg = "[QueryKeeper] ▶ ExpectTime X FAILED - " + methodName +
                    " took " + duration + "ms (expected <= " + expected + "ms)";
            finalLog.add(msg);
            finalFailures.add(new AssertionError("Execution time exceeded: " +
                    duration + "ms > " + expected + "ms"));
        } else {
            finalLog.add("[QueryKeeper] ▶ ExpectTime ✓ PASSED - " + methodName +
                    " took " + duration + "ms (expected <= " + expected + "ms)");
        }
    }
}
