package com.kokakiwi.mclauncher.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.utils.ProfileListModel;
import com.kokakiwi.mclauncher.utils.ProfileManager.Profile;

public class ProfilesPanel extends JDialog
{
    private static final long   serialVersionUID = 7927049760839380416L;
    private final LauncherFrame launcherFrame;
    
    private final JPanel        contentPanel     = new JPanel();
    private final Profile       previousProfile;
    private final JList<Object> list;
    
    /**
     * Create the dialog.
     */
    public ProfilesPanel(LauncherFrame parent)
    {
        super(parent);
        setTitle("Profiles Options");
        setResizable(false);
        launcherFrame = parent;
        previousProfile = launcherFrame.profiles.getCurrentProfile();
        
        setModal(true);
        setBounds(100, 100, 400, 233);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(null);
        
        list = new JList<Object>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(new LineBorder(new Color(0, 0, 0)));
        list.setBounds(10, 11, 275, 146);
        refreshList();
        list.setSelectedValue(launcherFrame.profiles.getCurrentProfile(), true);
        contentPanel.add(list);
        
        final JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                final ProfileAddPanel dialog = new ProfileAddPanel(
                        launcherFrame, ProfilesPanel.this,
                        ProfileAddPanel.Type.ADD);
                dialog.setTitle("Add profile");
                dialog.setVisible(true);
            }
        });
        btnAdd.setBounds(295, 8, 89, 23);
        contentPanel.add(btnAdd);
        
        final JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                final ProfileAddPanel dialog = new ProfileAddPanel(
                        launcherFrame, ProfilesPanel.this,
                        ProfileAddPanel.Type.EDIT);
                dialog.setTitle("Edit profile");
                dialog.setName(((Profile) list.getSelectedValue()).getName());
                dialog.setVisible(true);
            }
        });
        btnEdit.setBounds(295, 42, 89, 23);
        contentPanel.add(btnEdit);
        
        final JButton btnRemove = new JButton("Remove");
        btnRemove.setBounds(295, 76, 89, 23);
        contentPanel.add(btnRemove);
        {
            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                final JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0)
                    {
                        if (list.getSelectedValue() == null)
                        {
                            launcherFrame.profiles.setCurrentProfile("default");
                        }
                        else
                        {
                            launcherFrame.profiles
                                    .setCurrentProfile((Profile) list
                                            .getSelectedValue());
                        }
                        if (previousProfile != launcherFrame.profiles
                                .getCurrentProfile())
                        {
                            launcherFrame.refresh();
                            launcherFrame.loginForm.refresh();
                        }
                        ProfilesPanel.this.setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                final JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        ProfilesPanel.this.setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
    }
    
    public Profile getSelectedProfile()
    {
        return (Profile) list.getSelectedValue();
    }
    
    public void refreshList()
    {
        final ListModel<Object> model = new ProfileListModel(launcherFrame);
        list.setModel(model);
    }
}
