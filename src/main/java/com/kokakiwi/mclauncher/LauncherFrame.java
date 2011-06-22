package com.kokakiwi.mclauncher;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URLEncoder;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.kokakiwi.mclauncher.core.Launcher;
import com.kokakiwi.mclauncher.graphics.LoginForm;
import com.kokakiwi.mclauncher.utils.Configuration;
import com.kokakiwi.mclauncher.utils.Utils;

public class LauncherFrame extends Frame {
	private static final long serialVersionUID = -439450888759860507L;

	public Configuration config = new Configuration();

	public JPanel panel;
	public LoginForm loginForm;
	public Launcher launcher;

	public LauncherFrame() {
		super();
		config.load(Utils.getResourceAsStream("config/launcher.properties"));
		setTitle(config.get("windowTitle"));
		setBackground(Color.BLACK);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(854, 480));

		loginForm = new LoginForm(this);
		panel.add(loginForm);

		setLayout(new BorderLayout());
		add(panel, "Center");

		pack();
		setLocationRelativeTo(null);

		try {
			setIconImage(ImageIO.read(Utils
					.getResourceAsStream("res/favicon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent paramWindowEvent) {
				new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(30000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("FORCING EXIT!");
						System.exit(0);
					}
				}.start();
				System.exit(0);
			}

		});
	}
	
	public void login()
	{
		if(config.get("offlineMode").equals("true")){
			config.set("latestVersion", null);
			config.set("userName", loginForm.getUserName());
			runGame();
		}else {
			try {
				loginForm.setStatusText("Logging in...");
				String parameters = "user=" + URLEncoder.encode(loginForm.getUserName(), "UTF-8")
				+ "&password=" + URLEncoder.encode(new String(loginForm.getPassword()), "UTF-8")
				+ "&version=" + 13;
				String result = Utils.executePost(config.get("loginURL"), parameters);
				if(result == null)
				{
					//TODO Can't connect to login site. Offline mode?
					loginForm.setStatusText("Can't connect to login website.");
					return;
				}
				if(!result.contains(":"))
				{
					//TODO Error during login
					loginForm.setStatusText("Error during login.");
					return;
				}
				String[] values = result.split(":");
				config.set("latestVersion", values[0].trim());
				config.set("downloadTicket", values[1].trim());
				config.set("userName", values[2].trim());
				config.set("sessionID", values[3].trim());
				
				runGame();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	
	public void doLogin()
	{
		new Thread() {
			public void run()
			{
				LauncherFrame.this.login();
			}
		}.start();
	}
	
	public void loginError()
	{
		
	}
	
	public void runGame()
	{
		this.launcher = new Launcher(this);
		this.launcher.init();
		
		removeAll();
		add(this.launcher, "Center");
		validate();
		
		this.launcher.start();
		
		setTitle("Minecraft");
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		LauncherFrame launcherFrame = new LauncherFrame();
		launcherFrame.setVisible(true);
		launcherFrame.config.set("stand-alone", "true");
		if (args.length >= 3) {
			String ip = args[2];
			String port = "25565";
			if (ip.contains(":")) {
				String[] parts = ip.split(":");
				ip = parts[0];
				port = parts[1];
			}

			launcherFrame.config.set("server", ip);
			launcherFrame.config.set("port", port);
		}
		if(args.length >= 1)
		{
			launcherFrame.loginForm.userName.setText(args[0]);
			if(args.length >= 2)
			{
				launcherFrame.loginForm.password.setText(args[1]);
				launcherFrame.doLogin();
			}
		}
	}
}
