package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.system.info.server.ServerInfo;

import java.util.UUID;

public interface CheckOperations {

    boolean isPlayerRegistered(UUID player);

    boolean isPlayerRegistered(UUID player, UUID server);

    boolean doesWebUserExists(String username);

    @Deprecated
    default boolean isPlayerRegisteredOnThisServer(UUID player) {
        return isPlayerRegistered(player, ServerInfo.getServerUUID_Old());
    }

    boolean isServerInDatabase(UUID serverUUID);
}
