package com.app;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class DatabaseHelper {
    static Connection getDatabaseConnection() throws ClassNotFoundException, SQLException {
        Class.forName(Resources.databaseDriverString);
        return DriverManager.getConnection(Resources.databaseConnectionString,Resources.user,Resources.password);
    }

    static ArrayList<String> getTableNames() throws SQLException, ClassNotFoundException {
        Connection databaseConnection  = getDatabaseConnection();
        Statement statement = databaseConnection.createStatement();
        ResultSet result = statement.executeQuery("select table_name from information_schema.tables" +
                " where table_schema = \"" + Resources.databaseName + "\"");
        ArrayList<String> tblNames = new ArrayList<>();
        while (result.next()){
            tblNames.add(result.getString(1));
        }
        result.close();
        statement.close();
        databaseConnection.close();
        return tblNames;

    }

    static boolean isValidTable(String tableName) throws Exception {
        ArrayList<String> tables = getTableNames();
        return tables.contains(tableName);
    }

    static ArrayList<String> getTableColumnNames(String tableName) throws Exception {

        if (isValidTable(tableName)){
            ArrayList<String> columnNames = new ArrayList<>();
            Connection databaseConnection  = getDatabaseConnection();
            Statement statement = databaseConnection.createStatement();
            ResultSet result = statement.executeQuery("select column_name from information_schema.columns" +
                    " where table_name = \"" + tableName + "\"");
            while (result.next()){
                columnNames.add(result.getString(1));
            }
            result.close();
            statement.close();
            databaseConnection.close();
            return columnNames;
        }
        return null;
    }

    static DefaultTableModel createTableModel(ResultSet results) throws Exception {
        ResultSetMetaData metaData = results.getMetaData();

        Vector<String> clmnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            clmnNames.add(metaData.getColumnName(column));
        }

        Vector<Vector<Object>> tableData = new Vector<Vector<Object>>();
        while (results.next()) {
            Vector<Object> vec = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vec.add(results.getObject(columnIndex));
            }
            tableData.add(vec);
        }

        return new DefaultTableModel(tableData, clmnNames);
    }
}
