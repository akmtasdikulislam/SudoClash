package com.sudoclash;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.sudoclash.ui.MainPanel;
import com.sudoclash.ui.Theme;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.installToolTipStyle();
            JFrame frame = new JFrame("SudoClash - Competitive Sudoku");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            MainPanel panel = new MainPanel();
            panel.setExitAction(() -> {
                frame.dispose();
                System.exit(0);
            });
            frame.setContentPane(panel);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    panel.exitApp();
                }
            });

            // Small laptops (and Windows display scaling) give less room, so never open bigger than the screen.
            Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            frame.setMinimumSize(new Dimension(Math.min(900, usable.width), Math.min(600, usable.height)));
            frame.setSize(Math.min(1150, usable.width), Math.min(820, usable.height));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
