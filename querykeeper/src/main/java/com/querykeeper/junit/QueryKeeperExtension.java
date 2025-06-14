package com.querykeeper.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querykeeper.annotation.ExpectDetachedAccess;
import com.querykeeper.annotation.ExpectNoDb;
import com.querykeeper.annotation.ExpectQuery;
import com.querykeeper.annotation.ExpectTime;
import com.querykeeper.collector.QueryKeeperContext;
import com.querykeeper.engine.DetachedAccessAssertionEngine;
import com.querykeeper.engine.NoDbAssertionEngine;
import com.querykeeper.engine.NoTxAssertionEngine;
import com.querykeeper.engine.QueryAssertionEngine;
import com.querykeeper.engine.TimeAssertionEngine;

public class QueryKeeperExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(QueryKeeperExtension.class);

    private List<String> finalLog;
    private List<Throwable> finalFailures;

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        finalLog = new ArrayList<>();
        finalFailures = new ArrayList<>();
        QueryKeeperContext.clear();

        Method method = context.getRequiredTestMethod();
        context.getStore(NAMESPACE).put("startTime", System.currentTimeMillis());

        // ExpectNoTx
        NoTxAssertionEngine.assertNoTransaction(method, finalLog, finalFailures);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        Logger log = LoggerFactory.getLogger(QueryKeeperExtension.class);

        try {
            ExtensionContext.Store store = context.getStore(NAMESPACE);
            Long start = store.remove("startTime", Long.class);
            long duration = (start != null) ? System.currentTimeMillis() - start : -1;

            // ExpectTime
            ExpectTime expectTime = method.getAnnotation(ExpectTime.class);
            if (expectTime != null) {
                TimeAssertionEngine.assertExecutionTime(method, duration, expectTime.value(), finalLog, finalFailures);
            }

            // ExpectQuery
            ExpectQuery expectQuery = method.getAnnotation(ExpectQuery.class);
            if (expectQuery != null) {
                QueryAssertionEngine.assertQueries(method, expectQuery, finalLog, finalFailures);
            }

            // ExpectNoDb
            ExpectNoDb expectNoDb = method.getAnnotation(ExpectNoDb.class);
            if (expectNoDb != null) {
                NoDbAssertionEngine.assertNoDb(method, finalLog, finalFailures);
            }

            // ExpectDetachedAccess
            ExpectDetachedAccess expectDetachedAccess = method.getAnnotation(ExpectDetachedAccess.class);
            if (expectDetachedAccess != null) {
                DetachedAccessAssertionEngine.assertDetachedAccess(
                    QueryKeeperContext.getCurrent(),
                    finalLog,
                    finalFailures
                );
            }

        } finally {
            QueryKeeperContext.clear();
        }

        log.info("\n{}", String.join("\n", finalLog));

        if (!finalFailures.isEmpty()) {
            String summary = finalFailures.stream()
                    .map(Throwable::getMessage)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("Multiple assertion errors");
            throw new AssertionError(summary);
        }
    }
}
