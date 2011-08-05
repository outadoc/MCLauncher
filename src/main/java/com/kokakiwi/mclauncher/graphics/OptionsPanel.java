package com.kokakiwi.mclauncher.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.graphics.utils.TransparentLabel;
import com.kokakiwi.mclauncher.utils.Utils;

public class OptionsPanel extends JDialog
{
    private static final long   serialVersionUID = -8525835533380625378L;
    private final LauncherFrame launcherFrame;
    
    public class AL implements ActionListener
    {
        public JButton forceButton;
        
        public AL(JButton forceButton)
        {
            this.forceButton = forceButton;
        }
        
        public void actionPerformed(ActionEvent arg0)
        {
            launcherFrame.config.set("force-update", "true");
            forceButton.setText(launcherFrame.locale
                    .getString("options.willForce"));
            forceButton.setEnabled(false);
        }
        
    }
    
    public OptionsPanel(LauncherFrame parent)
    {
        super(parent);
        launcherFrame = parent;
        
        setModal(true);
        
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel label = new JLabel("Launcher options", 0);
        label.setBorder(new EmptyBorder(0, 0, 16, 0));
        label.setFont(new Font("Default", 1, 16));
        panel.add(label, "North");
        
        final JPanel optionsPanel = new JPanel(new BorderLayout());
        final JPanel labelPanel = new JPanel(new GridLayout(0, 1));
        final JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
        optionsPanel.add(labelPanel, "West");
        optionsPanel.add(fieldPanel, "Center");
        
        final JButton forceButton = new JButton(
                launcherFrame.locale.getString("options.forceUpdate"));
        if (launcherFrame.config.getString("force-update") != null)
        {
            forceButton.setEnabled(false);
            forceButton.setText(launcherFrame.locale
                    .getString("options.willForce"));
        }
        forceButton.addActionListener(new AL(forceButton));
        labelPanel.add(new JLabel(launcherFrame.locale
                .getString("options.forceUpdateLabel") + ": ", 4));
        fieldPanel.add(forceButton);
        
        labelPanel.add(new JLabel(launcherFrame.locale
                .getString("options.gameLocationLabel") + ": ", 4));
        final TransparentLabel dirLink = new TransparentLabel(Utils
                .getWorkingDirectory(launcherFrame).toString()) {
            private static final long serialVersionUID = 0L;
            
            @Override
            public void paint(Graphics g)
            {
                super.paint(g);
                
                int x = 0;
                int y = 0;
                
                final FontMetrics fm = g.getFontMetrics();
                final int width = fm.stringWidth(getText());
                final int height = fm.getHeight();
                
                if (getAlignmentX() == 2.0F)
                {
                    x = 0;
                }
                else if (getAlignmentX() == 0.0F)
                {
                    x = getBounds().width / 2 - width / 2;
                }
                else if (getAlignmentX() == 4.0F)
                {
                    x = getBounds().width - width;
                }
                y = getBounds().height / 2 + height / 2 - 1;
                
                g.drawLine(x + 2, y, x + width - 2, y);
            }
            
            @Override
            public void update(Graphics g)
            {
                paint(g);
            }
        };
        dirLink.setCursor(Cursor.getPredefinedCursor(12));
        dirLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent arg0)
            {
                try
                {
                    Utils.openLink(new URL("file://"
                            + Utils.getWorkingDirectory(launcherFrame)
                                    .getAbsolutePath().replaceAll(" ", "%20"))
                            .toURI());
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        dirLink.setForeground(new Color(2105599));
        
        fieldPanel.add(dirLink);
        
        panel.add(optionsPanel, "Center");
        
        final JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.add(new JPanel(), "Center");
        final JButton doneButton = new JButton("Done");
        doneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                OptionsPanel.this.setVisible(false);
            }
        });
        buttonsPanel.add(doneButton, "East");
        buttonsPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        
        panel.add(buttonsPanel, "South");
        
        add(panel);
        panel.setBorder(new EmptyBorder(16, 24, 24, 24));
        pack();
        setLocationRelativeTo(parent);
    }
}