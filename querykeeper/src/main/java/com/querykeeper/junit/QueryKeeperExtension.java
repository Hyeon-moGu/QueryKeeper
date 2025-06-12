package com.querykeeper.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querykeeper.annotation.ExpectLazyLoad;
import com.querykeeper.annotation.ExpectNoDb;
import com.querykeeper.annotation.ExpectQuery;
import com.querykeeper.annotation.ExpectTime;
import com.querykeeper.collector.QueryKeeperContext;
import com.querykeeper.engine.LazyLoadAssertionEngine;
import com.querykeeper.engine.NoDbAssertionEngine;
import com.querykeeper.engine.NoTxAssertionEngine;
import com.querykeeper.engine.QueryAssertionEngine;
import com.querykeeper.engine.TimeAssertionEngine;

public class QueryKeeperExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(QueryKeeperExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();

        QueryKeeperContext.clear();
        context.getStore(NAMESPACE).put("startTime", System.currentTimeMillis());

        // ExpectNoTx
        NoTxAssertionEngine.assertNoTransaction(method);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Method method = context.getRequiredTestMethod();
        Logger log = LoggerFactory.getLogger(QueryKeeperExtension.class);

        ExtensionContext.Store store = context.getStore(NAMESPACE);
        Long start = store.remove("startTime", Long.class);
        long duration = (start != null) ? System.currentTimeMillis() - start : -1;

        List<Throwable> failures = new ArrayList<>();

        // ExpectTime
        try {
            ExpectTime expectTime = method.getAnnotation(ExpectTime.class);
            if (expectTime != null) {
                TimeAssertionEngine.assertExecutionTime(method, duration, expectTime.value());
            }
        } catch (Throwable t) {
            failures.add(t);
        }

        // ExpectQuery
        try {
            ExpectQuery expectQuery = method.getAnnotation(ExpectQuery.class);
            if (expectQuery != null) {
                QueryAssertionEngine.assertQueries(method, expectQuery);
            }
        } catch (Throwable t) {
            failures.add(t);
        }

        // ExpectNoDb
        try {
            ExpectNoDb expectNoDb = method.getAnnotation(ExpectNoDb.class);
            if (expectNoDb != null) {
                NoDbAssertionEngine.assertNoDb(method);
            }
        } catch (Throwable t) {
            failures.add(t);
        }

        // ExpectLazyLoad
        try {
            ExpectLazyLoad config = method.getAnnotation(ExpectLazyLoad.class);
            if (config != null) {
                if (!config.entity().isEmpty()) {
                    LazyLoadAssertionEngine.validate(
                            config.entity(), config.maxCount(), config.includeException(),
                            QueryKeeperContext.getCurrent());
                } else {
                    LazyLoadAssertionEngine.validateAll(
                            config.maxCount(), config.includeException(),
                            QueryKeeperContext.getCurrent());
                }
            }
        } catch (Throwable t) {
            failures.add(t);
        }

        if (!failures.isEmpty()) {
            log.error("\n[QueryKeeper] ▶ {} failed with {} assertion(s)", method.getName(), failures.size());

            for (Throwable failure : failures) {
                log.error("{}", failure.getMessage());
            }

            String summary = failures.stream()
                    .map(Throwable::getMessage)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("Multiple assertion errors");

            throw new AssertionError(summary);
        }
    }

}
