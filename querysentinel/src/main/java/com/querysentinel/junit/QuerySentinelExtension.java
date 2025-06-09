package com.querysentinel.junit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.querysentinel.annotation.ExpectNoDb;
import com.querysentinel.annotation.ExpectQuery;
import com.querysentinel.annotation.ExpectTime;
import com.querysentinel.collector.QuerySentinelContext;
import com.querysentinel.engine.NoDbAssertionEngine;
import com.querysentinel.engine.NoTxAssertionEngine;
import com.querysentinel.engine.QueryAssertionEngine;
import com.querysentinel.engine.TimeAssertionEngine;

public class QuerySentinelExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Map<String, Long> startTimes = new HashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();

        QuerySentinelContext.clear();
        startTimes.put(context.getUniqueId(), System.currentTimeMillis());

        // ExpectNoTx
        NoTxAssertionEngine.assertNoTransaction(method);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Method method = context.getRequiredTestMethod();

        Throwable firstFailure = null;

        // ExpectTime
        try {
            ExpectTime expectTime = method.getAnnotation(ExpectTime.class);
            if (expectTime != null) {
                long end = System.currentTimeMillis();
                long duration = end - startTimes.getOrDefault(context.getUniqueId(), end);
                TimeAssertionEngine.assertExecutionTime(method, duration, expectTime.value());
            }
        } catch (Throwable t) {
            firstFailure = t;
        }

        // ExpectQuery
        try {
            ExpectQuery expectQuery = method.getAnnotation(ExpectQuery.class);
            if (expectQuery != null) {
                QueryAssertionEngine.assertQueries(method, expectQuery);
            }
        } catch (Throwable t) {
            if (firstFailure == null)
                firstFailure = t;
        }

        // ExpectNoDb
        try {
            ExpectNoDb expectNoDb = method.getAnnotation(ExpectNoDb.class);
            if (expectNoDb != null) {
                NoDbAssertionEngine.assertNoDb(method);
            }
        } catch (Throwable t) {
            if (firstFailure == null)
                firstFailure = t;
        }

        if (firstFailure != null) {
            if (firstFailure instanceof Exception) {
                throw (Exception) firstFailure;
            } else {
                throw new RuntimeException(firstFailure);
            }
        }
    }

}
