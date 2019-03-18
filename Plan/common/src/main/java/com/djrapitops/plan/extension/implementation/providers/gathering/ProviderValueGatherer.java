/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.extension.implementation.providers.gathering;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.DataProviderExtractor;
import com.djrapitops.plan.extension.implementation.PluginTab;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StorePluginTabTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StorePluginTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveInvalidResultsTransaction;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.logging.console.PluginLogger;

import java.util.Set;
import java.util.UUID;

/**
 * Object that can be called to place data about players to the database.
 *
 * @author Rsl1122
 */
public class ProviderValueGatherer {

    private final DataExtension extension;
    private final DataProviderExtractor extractor;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final PluginLogger logger;

    public ProviderValueGatherer(
            DataExtension extension,
            DataProviderExtractor extractor,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger
    ) {
        this.extension = extension;
        this.extractor = extractor;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.logger = logger;
    }

    public void storeExtensionInformation() {
        String pluginName = extractor.getPluginName();
        Icon pluginIcon = extractor.getPluginIcon();

        long time = System.currentTimeMillis();
        UUID serverUUID = serverInfo.getServerUUID();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new StoreIconTransaction(pluginIcon));
        database.executeTransaction(new StorePluginTransaction(pluginName, time, serverUUID, pluginIcon));
        for (PluginTab tab : extractor.getPluginTabs()) {
            database.executeTransaction(new StoreIconTransaction(tab.getTabIcon()));
            database.executeTransaction(new StorePluginTabTransaction(pluginName, serverUUID, tab));
        }

        database.executeTransaction(new RemoveInvalidResultsTransaction(pluginName, serverUUID, extractor.getInvalidatedMethods()));
        // TODO remove data in db that are updated with a 'false' condition
    }

    public void updateValues(UUID playerUUID, String playerName) {
        Set<String> providedConditions = gatherBooleanData(playerUUID, playerName);
        gatherValueData(playerUUID, playerName, providedConditions);
    }

    private Set<String> gatherBooleanData(UUID playerUUID, String playerName) {
        return new BooleanProviderValueGatherer(
                extractor.getPluginName(), extension,
                serverInfo.getServerUUID(), dbSystem.getDatabase(),
                extractor.getDataProviders(), logger
        ).gatherBooleanData(playerUUID, playerName);
    }

    private void gatherValueData(UUID playerUUID, String playerName, Set<String> providedConditions) {
        new NumberProviderValueGatherer(
                extractor.getPluginName(), extension,
                serverInfo.getServerUUID(), dbSystem.getDatabase(),
                extractor.getDataProviders(), logger
        ).gatherNumberData(playerUUID, playerName, providedConditions);
        new DoubleAndPercentageProviderValueGatherer(
                extractor.getPluginName(), extension,
                serverInfo.getServerUUID(), dbSystem.getDatabase(),
                extractor.getDataProviders(), logger
        ).gatherDoubleData(playerUUID, playerName, providedConditions);
        new StringProviderValueGatherer(
                extractor.getPluginName(), extension,
                serverInfo.getServerUUID(), dbSystem.getDatabase(),
                extractor.getDataProviders(), logger
        ).gatherStringData(playerUUID, playerName, providedConditions);
    }
}