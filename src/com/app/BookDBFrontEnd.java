package com.app;

import javax.swing.*;
import java.awt.*;

public class BookDBFrontEnd {
    static void runFrontEnd() throws Exception {
        JFrame mainFrame = new FrontEndUI("Test");
        mainFrame.setSize(new Dimension(1000,500));
        mainFrame.setResizable(false);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

    }
}
