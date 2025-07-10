package com.market.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        String currentDatabase = DatabaseContextHolder.getCurrentDatabase();
        if (currentDatabase == null) {
            System.out.println("No database key set, using default database: my_database_1");
            return "my_database_1"; // Default database key
        }

        return currentDatabase;
    }
}