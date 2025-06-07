package com.querysentinel.junit;

import com.querysentinel.annotation.ExpectNoDb;
import com.querysentinel.annotation.ExpectQuery;
import com.querysentinel.annotation.ExpectTime;
import com.querysentinel.collector.QuerySentinelContext;
import com.querysentinel.engine.NoDbAssertionEngine;
import com.querysentinel.engine.QueryAssertionEngine;
import com.querysentinel.engine.TimeAssertionEngine;
import com.querysentinel.reporter.QueryReporter;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class QuerySentinelExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Map<String, Long> startTimes = new HashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        QuerySentinelContext.clear();
        startTimes.put(context.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();

        // ExpectTime
        ExpectTime expectTime = method.getAnnotation(ExpectTime.class);
        if (expectTime != null) {
            long end = System.currentTimeMillis();
            long duration = end - startTimes.getOrDefault(context.getUniqueId(), end);
            TimeAssertionEngine.assertExecutionTime(method, duration, expectTime.value());
        }

        // ExpectQuery
        ExpectQuery expectQuery = method.getAnnotation(ExpectQuery.class);
        if (expectQuery != null) {
            QueryAssertionEngine.assertQueries(expectQuery);
            QueryReporter.printReport(context.getDisplayName());
        }

        // ExpectNoDb
        ExpectNoDb expectNoDb = method.getAnnotation(ExpectNoDb.class);
        if (expectNoDb != null) {
            NoDbAssertionEngine.assertNoDb(method);
        }
    }
}
