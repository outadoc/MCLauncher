package com.kokakiwi.mclauncher.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.graphics.utils.LogoPanel;
import com.kokakiwi.mclauncher.graphics.utils.TexturedPanel;
import com.kokakiwi.mclauncher.graphics.utils.TransparentButton;
import com.kokakiwi.mclauncher.graphics.utils.TransparentCheckbox;
import com.kokakiwi.mclauncher.graphics.utils.TransparentLabel;
import com.kokakiwi.mclauncher.graphics.utils.TransparentPanel;
import com.kokakiwi.mclauncher.utils.ClassesUtils;

public class LoginForm extends JPanel {
	private static final long serialVersionUID = -2684390357579600827L;
	
	private LauncherFrame launcherFrame;
	
	private JScrollPane scrollPane = null;
	public JTextField userName = new JTextField(20);
	public JPasswordField password = new JPasswordField(20);
	private TransparentCheckbox rememberBox = new TransparentCheckbox("Remember password");
	private TransparentButton launchButton = new TransparentButton("Login");
	private TransparentButton optionsButton = new TransparentButton("Options");
	//private TransparentButton retryButton = new TransparentButton("Try again");
	//private TransparentButton offlineButton = new TransparentButton("Play offline");
	private TransparentLabel statusText = new TransparentLabel("", 0);
	
	public LoginForm(LauncherFrame launcherFrame)
	{
		this.launcherFrame = launcherFrame;
		
		setLayout(new BorderLayout());
		
		add(buildMainLoginPanel(), "Center");
	}
	
	private JPanel buildMainLoginPanel()
	{
		JPanel panel = new TransparentPanel(new BorderLayout());
		
		panel.add(getUpdateNews(), "Center");
		
		JPanel southPanel = new TexturedPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(new LogoPanel(), "West");
		southPanel.add(this.statusText, "Center");
		southPanel.add(center(buidLoginPanel()), "East");
		southPanel.setPreferredSize(new Dimension(100, 100));
		
		panel.add(southPanel, "South");
		
		this.launchButton.addActionListener(new ClassesUtils.LaunchActionListener(launcherFrame));
		this.userName.addActionListener(new ClassesUtils.LaunchActionListener(launcherFrame));
		this.password.addActionListener(new ClassesUtils.LaunchActionListener(launcherFrame));
		this.optionsButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent paramActionEvent) {
				new OptionsPanel(LoginForm.this.launcherFrame).setVisible(true);
			}
		});
		
		return panel;
	}
	
	private JPanel buidLoginPanel()
	{
		TransparentPanel panel = new TransparentPanel();
		BorderLayout layout = new BorderLayout();
		layout.setHgap(0);
		layout.setVgap(8);
		panel.setLayout(layout);
		GridLayout gl1 = new GridLayout(0, 1);
		gl1.setVgap(2);
		GridLayout gl2 = new GridLayout(0, 1);
		gl2.setVgap(2);
		GridLayout gl3 = new GridLayout(0, 1);
		gl3.setVgap(2);
		
		TransparentPanel titles = new TransparentPanel(gl1);
		TransparentPanel values = new TransparentPanel(gl2);

		titles.add(new TransparentLabel("Username:", 4));
		titles.add(new TransparentLabel("Password:", 4));
		titles.add(new TransparentLabel("", 4));
		
		values.add(this.userName);
		values.add(this.password);
		values.add(this.rememberBox);
		
		panel.add(titles, "West");
		panel.add(values, "Center");
		
		TransparentPanel loginPanel = new TransparentPanel(new BorderLayout());
		
		TransparentPanel third = new TransparentPanel(gl3);
		third.add(this.optionsButton);
		third.add(this.launchButton);
		third.add(new TransparentPanel());
		
		third.setInsets(0, 10, 0, 10);
		titles.setInsets(0, 0, 0, 4);
		
		loginPanel.add(third, "Center");
		
		panel.add(loginPanel, "East");
		
		return panel;
	}
	
	private JScrollPane getUpdateNews()
	{
		if(this.scrollPane != null) return this.scrollPane;
		try {
			final JTextPane editorPane = new JTextPane();
			editorPane.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center>Loading update news..</center></font></body></html>");
			editorPane.addHyperlinkListener(new HyperlinkListener() {
				
				public void hyperlinkUpdate(HyperlinkEvent he) {
					if(he.getEventType() == EventType.ACTIVATED)
					{
						try {
							editorPane.setPage(he.getURL());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			new ClassesUtils.BrowserThread(editorPane, launcherFrame.config.get("browserHomeURL")).start();
			editorPane.setBackground(Color.DARK_GRAY);
			editorPane.setEditable(false);
			this.scrollPane = new JScrollPane(editorPane);
			this.scrollPane.setBorder(null);
			editorPane.setMargin(null);
			
			this.scrollPane.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return this.scrollPane;
	}
	
	private Component center(Component c) {
		TransparentPanel tp = new TransparentPanel(new GridBagLayout());
		tp.add(c);
		return tp;
	}
	
	public String getUserName()
	{
		return userName.getText();
	}
	
	public char[] getPassword()
	{
		return password.getPassword();
	}
	
	public void setStatusText(String message)
	{
		this.statusText.setText(message);
	}
}
