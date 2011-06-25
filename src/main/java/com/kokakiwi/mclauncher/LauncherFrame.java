package com.kokakiwi.mclauncher;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.kokakiwi.mclauncher.core.Launcher;
import com.kokakiwi.mclauncher.graphics.LoginForm;
import com.kokakiwi.mclauncher.utils.Configuration;
import com.kokakiwi.mclauncher.utils.LocalString;
import com.kokakiwi.mclauncher.utils.StringFormatter;
import com.kokakiwi.mclauncher.utils.Utils;

public class LauncherFrame extends Frame {
	private static final long serialVersionUID = -439450888759860507L;

	public Configuration config = new Configuration();

	public JPanel panel;
	public LoginForm loginForm;
	public Launcher launcher;
	public LocalString locale;

	public LauncherFrame() {
		super();
		
		config.load(Utils.getResourceAsStream("config/config.yml"), "Yaml");
		config.load(Utils.getResourceAsStream("config/launcher.properties"));
		
		File configFile = new File("./config.yml");
		
		if(!configFile.exists())
		{
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		config.load(configFile);
		
		locale = new LocalString(this, config.getStringList("launcher.langs"));
		
		setTitle(config.getString("launcher.windowTitle"));
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
	
	@SuppressWarnings("deprecation")
	public void login()
	{
		boolean offlineMode = config.getBoolean("launcher.offlineMode");
		
		if(offlineMode){
			playOffline();
		}else {
			try {
				loginForm.setStatusText(locale.getString("login.loggingIn"));
				Map<String, String> keys = new HashMap<String, String>();
				keys.put("USERNAME", URLEncoder.encode(loginForm.getUserName(), "UTF-8"));
				keys.put("PASSWORD", URLEncoder.encode(new String(loginForm.getPassword())));
				String parameters = StringFormatter.format(config.getString("launcher.loginParameters"), keys);
				String result = Utils.executePost(config.getString("launcher.loginURL"), parameters, config.getString("updater.keyFileName"));
				if(result == null)
				{
					loginForm.askOfflineMode();
					loginForm.setStatusText(locale.getString("launcher.loginError"));
					return;
				}
				if(!result.contains(":"))
				{
					if (result.trim().equals("Bad login")) {
						loginForm.setStatusText(locale.getString("launcher.badLogin"));
					}else if (result.trim().equals("Old version")) {
						loginForm.setStatusText(locale.getString("launcher.oldVersion"));
					}else {
						loginForm.setStatusText(result);
					}
					return;
				}
				String[] values = result.split(":");
				config.set("latestVersion", values[0].trim());
				config.set("downloadTicket", values[1].trim());
				config.set("userName", values[2].trim());
				config.set("sessionID", values[3].trim());
				loginForm.loginOk();
				
				runGame();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void playOffline()
	{
		config.set("latestVersion", "-1");
		config.set("userName", loginForm.getUserName());
		loginForm.loginOk();
		runGame();
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
