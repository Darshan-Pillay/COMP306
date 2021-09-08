package com.app;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReportPanelTwo extends JPanel {
    private final JTable reportTable;

    ReportPanelTwo() throws Exception {
        Connection databaseConnection = DatabaseHelper.getDatabaseConnection();
        Statement statement = databaseConnection.createStatement();
        ResultSet results = statement.executeQuery(Resources.reportTwoString);
        reportTable = new JTable(DatabaseHelper.createTableModel(results));
        results.close();
        statement.close();
        databaseConnection.close();
        reportTable.setDefaultEditor(Object.class,null);
        reportTable.setFillsViewportHeight(true);
        this.add(new JScrollPane(reportTable));
    }
}
