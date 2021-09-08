package com.app;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

public class InsertPanel extends JPanel {
    private JTable recordViewTable;
    private JPanel buttonPanel;
    private JButton addButton;
    private JButton filterButton;
    private JButton viewAllButton;
    private JComboBox<String> tableSelectionDropDown;
    private String currentlySelectedTable;
    private final JLabel tableLabel;

    InsertPanel() throws  Exception{
        currentlySelectedTable = "book";
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        Connection databaseConnection  = DatabaseHelper.getDatabaseConnection();
        Statement statement = databaseConnection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM " + currentlySelectedTable);
        recordViewTable = new JTable(DatabaseHelper.createTableModel(result));
        result.close();
        recordViewTable.setDefaultEditor(Object.class,null);
        recordViewTable.setFillsViewportHeight(true);
        this.add(new JScrollPane(recordViewTable));

        addButton = new JButton("Add A New Record");
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
        addButton.addActionListener(e -> {
            try {
                addRecord();
            } catch (SQLException | ClassNotFoundException throwables) {

            }
        });
        buttonPanel.add(addButton);
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
        ((DefaultTableModel)recordViewTable.getModel()).setDataVector(tableData,columnNames);
        ((DefaultTableModel)recordViewTable.getModel()).fireTableDataChanged();

    }

    private void filterRows() throws SQLException, ClassNotFoundException {
        Vector<String> columnNames = new Vector<>();
        for (int columnIndex = 0; columnIndex < recordViewTable.getColumnCount(); columnIndex++){
            columnNames.add(recordViewTable.getColumnName(columnIndex));
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
        ((DefaultTableModel)recordViewTable.getModel()).setDataVector(filteredRowData,columnNames);
        ((DefaultTableModel)recordViewTable.getModel()).fireTableDataChanged();
    }

    private void addRecord() throws SQLException, ClassNotFoundException {
        String insertQuery = "INSERT INTO " + currentlySelectedTable + " (";
        String valuesClause = "VALUES (";
        boolean allNecessaryDataAvailable = true;
        for (int columnIndex = 1; columnIndex < recordViewTable.getColumnCount(); columnIndex++){
            insertQuery = insertQuery + recordViewTable.getColumnName(columnIndex);
            String userInput = JOptionPane.showInputDialog(null,"Enter a " + recordViewTable.getColumnName(columnIndex));
            if (userInput == null || userInput.length() == 0){
                allNecessaryDataAvailable = false;
                break;
            }
            valuesClause = valuesClause + "'" + userInput + "'";
            if (columnIndex < recordViewTable.getColumnCount() - 1){
                insertQuery = insertQuery + ",";
                valuesClause = valuesClause + ",";
            }

        }
        valuesClause = valuesClause + ")";
        insertQuery = insertQuery + ")";
        if (allNecessaryDataAvailable){
            insertQuery = insertQuery + " " + valuesClause;
            Connection databaseConnection = DatabaseHelper.getDatabaseConnection();
            Statement statement = databaseConnection.createStatement();
            int rowsAffected = statement.executeUpdate(insertQuery);
            if (rowsAffected > 0){
                updateTable();
            }
            databaseConnection.close();
            statement.close();
        }
    }


}
