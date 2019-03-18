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
package com.djrapitops.plan.extension.implementation.storage.transactions.results;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.ExtensionPlayerValues;
import com.djrapitops.plan.db.sql.tables.ExtensionPluginTable;
import com.djrapitops.plan.db.sql.tables.ExtensionProviderTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.AND;
import static com.djrapitops.plan.db.sql.parsing.Sql.WHERE;

/**
 * Transaction to remove method results that correspond to {@link com.djrapitops.plan.extension.annotation.InvalidateMethod} annotations.
 *
 * @author Rsl1122
 */
public class RemoveInvalidResultsTransaction extends Transaction {

    private final String pluginName;
    private final UUID serverUUID;
    private final Collection<String> invalidatedMethods;

    public RemoveInvalidResultsTransaction(String pluginName, UUID serverUUID, Collection<String> invalidatedMethods) {
        this.pluginName = pluginName;
        this.serverUUID = serverUUID;
        this.invalidatedMethods = invalidatedMethods;
    }

    @Override
    protected void performOperations() {
        for (String invalidatedMethod : invalidatedMethods) {
            execute(deleteInvalidMethodResults(invalidatedMethod));
            execute(deleteInvalidMethodProvider(invalidatedMethod));
        }
    }

    private Executable deleteInvalidMethodResults(String invalidMethod) {
        String sql = "DELETE FROM " + ExtensionPlayerValues.TABLE_NAME +
                WHERE + ExtensionPlayerValues.PROVIDER_ID + "=" + ExtensionProviderTable.STATEMENT_SELECT_PROVIDER_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                ExtensionProviderTable.set3PluginValuesToStatement(statement, 1, invalidMethod, pluginName, serverUUID);
            }
        };
    }

    private Executable deleteInvalidMethodProvider(String invalidMethod) {
        String sql = "DELETE FROM " + ExtensionProviderTable.TABLE_NAME +
                WHERE + ExtensionProviderTable.PROVIDER_NAME + "=?" +
                AND + ExtensionProviderTable.PLUGIN_ID + ExtensionPluginTable.STATEMENT_SELECT_PLUGIN_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, invalidMethod);
                ExtensionPluginTable.set2PluginValuesToStatement(statement, 2, pluginName, serverUUID);
            }
        };
    }
}