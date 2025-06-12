package com.querykeeper.engine;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querykeeper.collector.QueryKeeperContext;

public class LazyLoadAssertionEngine {
    private static final Logger log = LoggerFactory.getLogger(LazyLoadAssertionEngine.class);

    public static void validate(String entity, int maxCount, boolean includeException, QueryKeeperContext ctx) {
        int actual = ctx.getLazyLoadCount(entity);
        boolean hadLazyException = ctx.hadLazyLoadException(entity);

        if (actual > maxCount || (includeException && hadLazyException)) {
            throw new AssertionError(String.format(
                    "\n[QueryKeeper] ▶ ExpectLazyLoad X FAILED\nEntity: %s\nExpected Max: %d\nActual: %d\nException: %s",
                    entity, maxCount, actual, hadLazyException));
        }
        log.info("\n[QueryKeeper] ▶ ExpectLazyLoad ✓ PASSED - {} lazy-loaded {} time(s)", entity, actual);
    }

    public static void validateAll(int maxCount, boolean includeException, QueryKeeperContext ctx) {
        Set<String> entities = ctx.getAllEntitiesWithLazyLoad();

        if (entities.isEmpty()) {
            log.info("\n[QueryKeeper] ▶ ExpectLazyLoad ✓ PASSED - No lazy loads or exceptions detected");
            return;
        }

        for (String entity : entities) {
            int actual = ctx.getLazyLoadCount(entity);
            boolean hadException = ctx.hadLazyLoadException(entity);

            if (actual > maxCount || (includeException && hadException)) {
                throw new AssertionError(String.format(
                        "\n[QueryKeeper] ▶ ExpectLazyLoad X FAILED - Entity: %s, Expected Max: %d, Actual: %d, Exception: %s",
                        entity, maxCount, actual, hadException));
            }

            log.info("\n[QueryKeeper] ▶ ExpectLazyLoad ✓ PASSED - {} lazy-loaded {} time(s)", entity, actual);
        }
    }
}
