package com.querykeeper.engine;

import java.util.List;
import java.util.Set;

import com.querykeeper.collector.QueryKeeperContext;
import com.querykeeper.collector.QueryKeeperContext.DetachedAccessInfo;

public class DetachedAccessAssertionEngine {

    public static void assertDetachedAccess(
            QueryKeeperContext ctx,
            List<String> finalLog,
            List<Throwable> finalFailures) {

        Set<String> entities = ctx.getEntitiesWithDetachedAccess();

        if (entities.isEmpty()) {
            finalLog.add("[QueryKeeper] ▶ ExpectDetachedAccess ✓ PASSED - No detached access exceptions detected");
            return;
        }

        for (String entity : entities) {
            DetachedAccessInfo info = ctx.getDetachedAccessInfo(entity);
            if (info == null) continue;

            StringBuilder msg = new StringBuilder();
            msg.append("[QueryKeeper] ▶ ExpectDetachedAccess X FAILED")
               .append(" - Entity: ").append(info.entity)
               .append("\n  • Field: ").append(info.field);

            if (info.fullPath != null)
                msg.append("\n  • Access Path: ").append(info.fullPath);
            if (info.rootEntity != null)
                msg.append("\n  • Root Entity: ").append(info.rootEntity);

            finalLog.add(msg.toString());
            finalFailures.add(new AssertionError(msg.toString()));
        }
    }
}
