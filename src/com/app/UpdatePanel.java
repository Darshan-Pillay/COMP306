package com.app;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

public class UpdatePanel extends JPanel {
    private JTable updateSourceTable;
    private JPanel buttonPanel;
    private JButton updateButton;
    private JButton filterButton;
    private JButton viewAllButton;
    private JComboBox<String> tableSelectionDropDown;
    private String currentlySelectedTable;
    private final JLabel tableLabel;

    UpdatePanel() throws Exception{
        currentlySelectedTable = "book";
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        Connection databaseConnection  = DatabaseHelper.getDatabaseConnection();
        Statement statement = databaseConnection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM " + currentlySelectedTable);
        updateSourceTable = new JTable(DatabaseHelper.createTableModel(result));
        result.close();
        updateSourceTable.setDefaultEditor(Object.class,null);
        updateSourceTable.setFillsViewportHeight(true);
        this.add(new JScrollPane(updateSourceTable));

        updateButton = new JButton("Update Selected Rows");
        filterButton = new JButton("Filter Records");
        viewAllButton = new JButton("View All Records");
        buttonPanel = new JPanel();
        tableLabel = new JLabel("Current Table");
        buttonPanel.add(tableLabel);
        ResultSet tableNames = statement.executeQuery("SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = \'" + Resources.databaseName + "\'");

        ArrayList<String> tables = new ArrayList<>();
        while (tableNames.next()){
            tables.add((String) tableNames.getObject(1));
        }
        tableNames.close();
        statement.close();
        databaseConnection.close();
        String[] arrayTables = tables.toArray(new String[0]);
        tableSelectionDropDown  = new JComboBox<>(arrayTables);
        tableSelectionDropDown.addActionListener(e ->{
            if (String.valueOf(tableSelectionDropDown.getSelectedItem()).compareTo(currentlySelectedTable) != 0){
                currentlySelectedTable = String.valueOf(tableSelectionDropDown.getSelectedItem());
                try {
                    updateTable();
                } catch (SQLException | ClassNotFoundException throwables) {

                }
            }

        });
        buttonPanel.add(tableSelectionDropDown);
        updateButton.addActionListener(e -> {
            try {
                updateSelectedRows(updateSourceTable.getSelectedRows());
            } catch (SQLException | ClassNotFoundException throwables) {

            }
        });
        buttonPanel.add(updateButton);
        filterButton.addActionListener(e -> {
            try {
                filterRows();
            } catch (SQLException | ClassNotFoundException throwables) {

            }
        });
        buttonPanel.add(filterButton);
        viewAllButton.addActionListener(e -> {
            try {
                updateTable();
            } catch (SQLException | ClassNotFoundException throwables) {

            }
        });
        buttonPanel.add(viewAllButton);
        this.add(buttonPanel);

    }

    private int[] getSelectedRecordsPrimaryKeyArray(int[] selectedRowIndices){
        if (selectedRowIndices != null){
            int[] primaryKeys = new int[selectedRowIndices.length];
            int indexCount = 0;
            for (int selectedRowIndex : selectedRowIndices){
                primaryKeys[indexCount] = (int) updateSourceTable.getValueAt(selectedRowIndex,0);
                indexCount++;
            }
            return primaryKeys;
        }
        return null;
    }

    private void updateSelectedRows(int[] selectedRowIndices) throws SQLException, ClassNotFoundException {
        int[] primaryKeys = getSelectedRecordsPrimaryKeyArray(selectedRowIndices);
        if (primaryKeys != null && primaryKeys.length > 0){
            Vector<String> columnNames = new Vector<>();
            for (int columnIndex = 0; columnIndex < updateSourceTable.getColumnCount(); columnIndex++){
                columnNames.add(updateSourceTable.getColumnName(columnIndex));
            }
           String updateQueryString = "UPDATE " + currentlySelectedTable;
           String setClause = "SET ";
           for (int columnIndex = 1; columnIndex < updateSourceTable.getColumnCount(); columnIndex++){
               String userUpdateData = JOptionPane.showInputDialog(null,"(Optional) Enter a new  " + columnNames.get(columnIndex));
               if (userUpdateData.length() != 0){
                   if (setClause.length() > 4){
                       setClause = setClause + ", " + columnNames.get(columnIndex) + "=" + "'" + userUpdateData + "'";
                   }
                   else {
                       setClause = setClause + columnNames.get(columnIndex) + "=" + "'" + userUpdateData + "'";
                   }
               }
           }
           if (setClause.length() > 4){
               updateQueryString = updateQueryString + " " + setClause;
               String whereClause = "WHERE " + columnNames.get(0);
               String inClause = "IN (";
               for (int selectedItemNo = 0; selectedItemNo < primaryKeys.length; selectedItemNo++){
                    inClause = inClause + "'" + primaryKeys[selectedItemNo] + "'";
                    if (selectedItemNo != primaryKeys.length - 1){
                        inClause = inClause + ",";
                    }
               }
               inClause = inClause + ")";
               updateQueryString = updateQueryString + " " + whereClause + " " + inClause;
               Connection databaseConnection = DatabaseHelper.getDatabaseConnection();
               Statement statement = databaseConnection.createStatement();
               int numberOfRowsAffected = statement.executeUpdate(updateQueryString);
               if (numberOfRowsAffected > 0){
                   ResultSet updatedRecords = statement.executeQuery("SELECT * FROM " + currentlySelectedTable
                   + " " + whereClause + " " + inClause);
                   Vector<Vector<Object>> updatedRowData = new Vector<>();

                   while (updatedRecords.next()){
                       Vector<Object> vec = new Vector<>();
                       for (int column = 0; column < columnNames.size(); column++){
                           vec.add(updatedRecords.getObject(column + 1));
                       }
                       updatedRowData.add(vec);
                   }
                   updatedRecords.close();
                   ((DefaultTableModel)updateSourceTable.getModel()).setDataVector(updatedRowData,columnNames);
                   ((DefaultTableModel)updateSourceTable.getModel()).fireTableDataChanged();
               }
               statement.close();
               databaseConnection.close();
           }
        }
    }

    public void updateTable() throws SQLException, ClassNotFoundException {
        Vector<String> columnNames = new Vector<>();
        Connection dataBaseConnection = DatabaseHelper.getDatabaseConnection();
        Statement statement = dataBaseConnection.createStatement();
        ResultSet rows = statement.executeQuery("SELECT * FROM " + currentlySelectedTable);
        for (int column = 0; column < rows.getMetaData().getColumnCount(); column++){
            columnNames.add(rows.getMetaData().getColumnName(column + 1));
        }
        Vector<Vector<Object>> tableData = new Vector<Vector<Object>>();
        while(rows.next()){
            Vector<Object> vec = new Vector<>();
            for (int columnIndex = 0; columnIndex < columnNames.size();columnIndex++){
                vec.add(rows.getObject(columnIndex + 1));
            }
            tableData.add(vec);
        }
        dataBaseConnection.close();
        statement.close();
        rows.close();
        ((DefaultTableModel)updateSourceTable.getModel()).setDataVector(tableData,columnNames);
        ((DefaultTableModel)updateSourceTable.getModel()).fireTableDataChanged();

    }

    private void filterRows() throws SQLException, ClassNotFoundException {
        Vector<String> columnNames = new Vector<>();
        for (int columnIndex = 0; columnIndex < updateSourceTable.getColumnCount(); columnIndex++){
            columnNames.add(updateSourceTable.getColumnName(columnIndex));
        }
        String queryString = "SELECT * FROM " + currentlySelectedTable;
        String whereClause = "WHERE ";

        for (int column = 0; column < columnNames.size(); column++){
            String userFilterInput = JOptionPane.showInputDialog(null, "(Optional) Enter a " + columnNames.get(column));
            if (userFilterInput.length() != 0){
                if (whereClause.length() > 6){
                    whereClause = whereClause + "AND " + columnNames.get(column) + "= '" + userFilterInput + "' ";
                }
                else {
                    whereClause = whereClause + columnNames.get(column) + "= '" + userFilterInput + "' ";
                }

            }
        }
        if (whereClause.length() > 6){
            queryString = queryString + " " + whereClause;
        }
        Connection databaseConnection = DatabaseHelper.getDatabaseConnection();
        Statement statement = databaseConnection.createStatement();
        ResultSet results = statement.executeQuery(queryString);

        Vector<Vector<Object>> filteredRowData = new Vector<>();
        while(results.next()){
            Vector<Object> vec = new Vector<>();
            for (int column = 0; column < columnNames.size(); column++){
                vec.add(results.getObject(column + 1));
            }
            filteredRowData.add(vec);
        }
        results.close();
        statement.close();
        databaseConnection.close();
        ((DefaultTableModel)updateSourceTable.getModel()).setDataVector(filteredRowData,columnNames);
        ((DefaultTableModel)updateSourceTable.getModel()).fireTableDataChanged();
    }
}
