/*
 * Copyright (C) 2020 Andreas Reichel<andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.manticore.h2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public final class MetaData {

    private final TreeMap<String, Catalog> catalogs = new TreeMap<>();
    private final DatabaseMetaData metaData;

    public MetaData(Connection con) throws SQLException {
        this.metaData = con.getMetaData();
    }

    public void build() throws SQLException {
        for (Catalog catalog : Catalog.getCatalogs(metaData)) {
            put(catalog);
        }

        for (Schema schema : Schema.getSchemas(metaData)) {
            put(schema);
        }

        for (Table table : Table.getTables(metaData)) {
            put(table);
            table.getColumns(metaData);

            // "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS",
            // "SYNONYM"
            if (table.tableType.equals("TABLE") || table.tableType.equals("SYSTEM TABLE")) {
                table.getIndices(metaData, true);
                table.getPrimaryKey(metaData);
            }
        }

    }

    public Catalog put(Catalog catalog) {
        return catalogs.put(catalog.tableCatalog.toUpperCase(), catalog);
    }

    public Map<String, Catalog> getCatalogs() {
        return Collections.unmodifiableMap(catalogs);
    }

    public Schema put(Schema schema) {
        Catalog catalog = catalogs.get(schema.tableCatalog.toUpperCase());
        return catalog.put(schema);
    }

    public Table put(Table table) {
        Catalog catalog = catalogs.get(table.tableCatalog.toUpperCase());
        Schema schema = catalog.get(table.tableSchema.toUpperCase());

        return schema.put(table);
    }
}
