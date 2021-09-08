package com.app;

import javax.swing.*;
import java.sql.SQLException;

public class FrontEndUI extends JFrame {
    private JTabbedPane tabPane;
    private InsertPanel insertPanel;
    private UpdatePanel updatePanel;
    private DeletePanel deletePanel;

    FrontEndUI(String frameTitle) throws Exception {
        if (frameTitle != null && frameTitle.length() > 0){
            this.setTitle(frameTitle);
        }
        else {
            this.setTitle("Untitled");
        }
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        tabPane = new JTabbedPane();
        insertPanel = new InsertPanel();
        updatePanel = new UpdatePanel();
        deletePanel = new DeletePanel();
        tabPane.add("Insert Records",insertPanel);
        tabPane.add("Update Records",updatePanel);
        tabPane.add("Delete Records",deletePanel);
        tabPane.add("AuthorNationality-BookFrequency",new ReportPanelOne());
        tabPane.add("Publisher-BookFrequency",new ReportPanelTwo());
        tabPane.add("BookSubject-BookFrequency",new ReportPanelThree());
        tabPane.addChangeListener(e -> {
            int currentTabIndex = tabPane.getSelectedIndex();
            String tabTitle = tabPane.getTitleAt(currentTabIndex);
            switch (tabTitle){
                case "Insert Records":
                    try {
                        insertPanel.updateTable();
                    } catch (SQLException | ClassNotFoundException throwables) {}
                    break;
                case "Delete Records":
                    try {
                        deletePanel.displayAllRecords();
                    } catch (SQLException | ClassNotFoundException throwables) {}
                    break;
                case "Update Records":
                    try {
                        updatePanel.updateTable();
                    } catch (SQLException | ClassNotFoundException throwables) {}
                    break;
            }
        });
        this.add(tabPane);
    }
}
