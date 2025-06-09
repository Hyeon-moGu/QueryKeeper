package com.querysentinel.engine;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.querysentinel.annotation.ExpectNoTx;

public class NoTxAssertionEngine {

    private static final Logger logger = LoggerFactory.getLogger(NoTxAssertionEngine.class);

    public static void assertNoTransaction(Method method) {
        ExpectNoTx annotation = method.getAnnotation(ExpectNoTx.class);
        if (annotation == null)
            return;

        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        boolean shouldFail = annotation.strict() ? active : (active && !readOnly);

        if (shouldFail) {
            logger.error("\n[QuerySentinel] ExpectNoTx ❌ FAILED - Transaction is active in {}()", method.getName());
            throw new AssertionError("Expected no transaction, but one was active.");
        } else {
            logger.info("\n[QuerySentinel] ExpectNoTx ✅ PASSED - No transaction in {}()", method.getName());
        }
    }
}
