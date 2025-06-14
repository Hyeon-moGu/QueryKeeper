package com.querykeeper.engine;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.querykeeper.annotation.ExpectNoTx;

public class NoTxAssertionEngine {

    private static final Logger logger = LoggerFactory.getLogger(NoTxAssertionEngine.class);

    public static void assertNoTransaction(Method method, List<String> finalLog, List<Throwable> finalFailures) {
        ExpectNoTx annotation = method.getAnnotation(ExpectNoTx.class);
        if (annotation == null)
            return;

        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        boolean shouldFail = annotation.strict() ? active : (active && !readOnly);

        if (shouldFail) {
            String msg = "[QueryKeeper] ▶ ExpectNoTx X FAILED - Transaction is active in " + method.getName() + "()";
            finalLog.add(msg);
            finalFailures.add(new AssertionError(msg));
        } else {
            finalLog.add("[QueryKeeper] ▶ ExpectNoTx ✓ PASSED - No transaction in " + method.getName() + "()");
        }
    }
}
