package com.app;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;

public class DeletePanel extends JPanel {
    private JTable deletionSourceTable;
    private JPanel buttonPanel;
    private JButton deleteButton;
    private JButton filterButton;
    private JButton viewAllButton;
    DeletePanel() throws Exception {
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        Connection databaseConnection  = DatabaseHelper.getDatabaseConnection();
        Statement statement = databaseConnection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM book");
        deletionSourceTable = new JTable(DatabaseHelper.createTableModel(result));
        result.close();
        statement.close();
        databaseConnection.close();
        deletionSourceTable.setDefaultEditor(Object.class,null);
        deletionSourceTable.setFillsViewportHeight(true);
        this.add(new JScrollPane(deletionSourceTable));

        deleteButton = new JButton("Delete Selected Rows");
        deleteButton.addActionListener(e -> {
            try {
                deleteSelectedRows(deletionSourceTable.getSelectedRows());
            } catch (Exception exception) {
            }
        });
        filterButton = new JButton("Filter Records");
        filterButton.addActionListener(e -> {
            try {
                filterRows();
            } catch (SQLException | ClassNotFoundException throwables) {
            }
        });
        viewAllButton = new JButton("View All Records");
        viewAllButton.addActionListener(e ->{
            try {
                displayAllRecords();
            } catch (SQLException | ClassNotFoundException throwables) {

            }
        });
        buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);
        buttonPanel.add(filterButton);
        buttonPanel.add(viewAllButton);
        this.add(buttonPanel);
    }

    private void deleteSelectedRows(int[] selectedRowIndices) throws Exception {
        String preparedDeleteQuery = "DELETE FROM book WHERE BookID = ?";
        if (selectedRowIndices != null){
            int[] primaryKeys = getSelectedRecordsPrimaryKeyArray(deletionSourceTable.getSelectedRows());
            Connection databaseConnection  = DatabaseHelper.getDatabaseConnection();
            PreparedStatement deleteSelectedRecordQuery = databaseConnection.prepareStatement(preparedDeleteQuery);
            DefaultTableModel newModel = (DefaultTableModel)deletionSourceTable.getModel();


            for (int primaryKey : primaryKeys) {
                deleteSelectedRecordQuery.setInt(1, primaryKey);
                deleteSelectedRecordQuery.executeUpdate();
            }
            deleteSelectedRecordQuery.close();
            databaseConnection.close();
            updateTable(getRowsNotSelected(selectedRowIndices));

        }
    }

    private int[] getRowsNotSelected(int[] selectedRowIndices){
        if (selectedRowIndices != null && selectedRowIndices.length > 0){
            int[] selectedCharacteristicVector = new int[deletionSourceTable.getRowCount()];
            for (int selectedRowIndex : selectedRowIndices) {
                selectedCharacteristicVector[selectedRowIndex] = 1;
            }
            return selectedCharacteristicVector;
        }
        return null;
    }

    private void updateTable(int[] deletedRowIndices){
        if (deletedRowIndices != null && deletedRowIndices.length != 0){
            Vector<String> columnNames = new Vector<>();
            for (int i = 0; i < deletionSourceTable.getColumnCount(); i++){
                columnNames.add(deletionSourceTable.getColumnName(i));
            }
            Vector<Vector<Object>> newTableData = new Vector<>();
            int indexCount = 0;
            for (int index: deletedRowIndices){
                if (index != 1){
                    Vector<Object> vec = new Vector<>();
                    for (int columnIndex = 0; columnIndex < columnNames.size();columnIndex++){
                        vec.add(deletionSourceTable.getModel().getValueAt(indexCount,columnIndex));
                    }
                    newTableData.add(vec);
                }
                indexCount++;
            }
            ((DefaultTableModel)deletionSourceTable.getModel()).setDataVector(newTableData,columnNames);
            ((DefaultTableModel)deletionSourceTable.getModel()).fireTableDataChanged();
        }
    }

    private int[] getSelectedRecordsPrimaryKeyArray(int[] selectedRowIndices){
        if (selectedRowIndices != null){
            int[] primaryKeys = new int[selectedRowIndices.length];
            int indexCount = 0;
            for (int selectedRowIndex : selectedRowIndices){
                primaryKeys[indexCount] = (int) deletionSourceTable.getValueAt(selectedRowIndex,0);
                indexCount++;
            }
            return primaryKeys;
        }
        return null;
    }

    private Integer getValidInt(String prompt){
        try{
            return Integer.parseInt(JOptionPane.showInputDialog(null,prompt));
        } catch (NumberFormatException e){
            return null;
        }
    }

    private String getValidString(String prompt){
        return JOptionPane.showInputDialog(null, prompt);
    }

    public void displayAllRecords() throws SQLException, ClassNotFoundException {
        Connection databaseConnection = DatabaseHelper.getDatabaseConnection();
        String query = "SELECT * FROM book";
        Statement statement = databaseConnection.createStatement();
        ResultSet records = statement.executeQuery(query);
        Vector<String> columnNames = new Vector<>();
        for (int columnIndex = 0; columnIndex < deletionSourceTable.getColumnCount(); columnIndex++){
            columnNames.add(deletionSourceTable.getColumnName(columnIndex));
        }
        Vector<Vector<Object>> tableData = new Vector<>();
        while(records.next()){
            Vector<Object> vec = new Vector<>();
            for (int column = 0; column < columnNames.size(); column++){
                vec.add(records.getObject(column + 1));
            }
            tableData.add(vec);
        }
        statement.close();
        records.close();
        databaseConnection.close();
        ((DefaultTableModel)deletionSourceTable.getModel()).setDataVector(tableData,columnNames);
        ((DefaultTableModel)deletionSourceTable.getModel()).fireTableDataChanged();
    }

    private void filterRows() throws SQLException, ClassNotFoundException {
        Integer BookID = getValidInt("(Optional) Enter a BookID");
        String BookTitle = getValidString("(Optional) Enter a Book Title");
        Integer AuthorID = getValidInt("(Optional) Enter a AuthorID");
        Integer PublisherID = getValidInt("(Optional) Enter a PublisherID");
        Integer SubjectID = getValidInt("(Optional) Enter a SubjectID");
        Integer GenreID = getValidInt("(Optional) Enter a GenreID");

        String whereClause = "WHERE ";
        if (BookID != null){
            whereClause = whereClause + " BookID=\'" + BookID + "\'";
        }
        if (BookTitle.length() > 0){
            if (whereClause.length() > 6){
                whereClause = whereClause + "AND BookTitle = \'" + BookTitle + "\'";
            }
            else {
                whereClause = whereClause +  "BookTitle = \'" + BookTitle + "\'";
            }
        }
        if (AuthorID != null){
            if (whereClause.length() > 6){
                whereClause = whereClause + "AND AuthorID = \'" + AuthorID + "\'";
            }
            else {
                whereClause = whereClause +  "AuthorID = \'" + AuthorID + "\'";
            }
        }
        if (PublisherID != null){
            if (whereClause.length() > 6){
                whereClause = whereClause + "AND PublisherID = \'" + PublisherID + "\'";
            }
            else {
                whereClause = whereClause +  "PublisherID = \'" + PublisherID + "\'";
            }
        }
        if (SubjectID != null){
            if (whereClause.length() > 6){
                whereClause = whereClause + "AND SubjectID = \'" + SubjectID + "\'";
            }
            else {
                whereClause = whereClause +  "SubjectID = \'" + SubjectID + "\'";
            }
        }
        if (GenreID != null){
            if (whereClause.length() > 6){
                whereClause = whereClause + "AND GenreID = \'" + GenreID + "\'";
            }
            else {
                whereClause = whereClause +  "GenreID = \'" + GenreID + "\'";
            }
        }
        if (whereClause.length() > 6){
            Connection databaseConnection = DatabaseHelper.getDatabaseConnection();
            String  selectQuery = "SELECT * FROM book " + whereClause;
            Statement filterStatement = databaseConnection.createStatement();
            ResultSet filteredData = filterStatement.executeQuery(selectQuery);
            Vector<String> columnNames = new Vector<>();
           for (int columnIndex = 0; columnIndex < deletionSourceTable.getColumnCount(); columnIndex++){
               columnNames.add(deletionSourceTable.getColumnName(columnIndex));
           }
           Vector<Vector<Object>> filteredRowData = new Vector<>();
           while(filteredData.next()){
               Vector<Object> vec = new Vector<>();
               for (int column = 0; column < columnNames.size(); column++){
                   vec.add(filteredData.getObject(column + 1));
               }
               filteredRowData.add(vec);
           }
           filteredData.close();
           filterStatement.close();
           databaseConnection.close();
           ((DefaultTableModel)deletionSourceTable.getModel()).setDataVector(filteredRowData,columnNames);
           ((DefaultTableModel)deletionSourceTable.getModel()).fireTableDataChanged();
        }
    }


}
