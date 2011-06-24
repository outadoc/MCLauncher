package com.kokakiwi.mclauncher.core;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.utils.State;
import com.kokakiwi.mclauncher.utils.Utils;

public class Launcher extends Applet implements Runnable, AppletStub,
		MouseListener {
	private static final long serialVersionUID = -2433230602156426362L;

	private LauncherFrame launcherFrame;
	public Applet applet;
	private Image bgImage;
	private int context = 0;
	private boolean active = false;
	private VolatileImage img;
	public GameUpdater updater;
	public GameLauncher launcher;
	public boolean pauseAskUpdate = false;
	private int percentage;
	private State state = State.INIT;
	private boolean hasMouseListener;
	public String subtaskMessage = "";
	public Map<String, String> customParameters = new HashMap<String, String>();

	public Launcher(LauncherFrame launcherFrame) {
		this.launcherFrame = launcherFrame;
	}

	public void init() {
		if (this.applet != null) {
			this.applet.init();
			return;
		}

		try {
			this.bgImage = ImageIO.read(
					Utils.getResourceAsStream("res/dirt.png"))
					.getScaledInstance(32, 32, 16);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(launcherFrame.config.getString("server") != null)
			this.customParameters.put("server", launcherFrame.config.getString("server"));
		
		if(launcherFrame.config.getString("port") != null)
			this.customParameters.put("port", launcherFrame.config.getString("port"));
		
		if(launcherFrame.config.getString("latestversion") != null)
			this.customParameters.put("latestVersion", launcherFrame.config.getString("latestVersion"));
		
		if(launcherFrame.config.getString("downloadticket") != null)
			this.customParameters.put("downloadTicket", launcherFrame.config.getString("downloadTicket"));
		
		if(launcherFrame.config.getString("sessionid") != null)
			this.customParameters.put("sessionID", launcherFrame.config.getString("sessionID"));
		
		this.customParameters.put("username", launcherFrame.config.getString("userName") == null ? "Player" : launcherFrame.config.getString("userName"));
		
		//this.customParameters.put("stand-alone", "true");

		this.updater = new GameUpdater(this.launcherFrame);
		this.launcher = new GameLauncher(this.launcherFrame);
	}

	public void mouseClicked(MouseEvent paramMouseEvent) {

	}

	public void mousePressed(MouseEvent me) {
		int x = me.getX() / 2;
		int y = me.getY() / 2;
		int w = getWidth() / 2;
		int h = getHeight() / 2;

		if (contains(x, y, w / 2 - 56 - 8, h / 2, 56, 20)) {
			removeMouseListener(this);
			this.updater.shouldUpdate = true;
			this.pauseAskUpdate = false;
			this.hasMouseListener = false;
		}
		if (contains(x, y, w / 2 + 8, h / 2, 56, 20)) {
			removeMouseListener(this);
			this.updater.shouldUpdate = false;
			this.pauseAskUpdate = false;
			this.hasMouseListener = false;
		}
	}

	public void mouseReleased(MouseEvent paramMouseEvent) {

	}

	public void mouseEntered(MouseEvent paramMouseEvent) {

	}

	public void mouseExited(MouseEvent paramMouseEvent) {

	}

	public void appletResize(int paramInt1, int paramInt2) {

	}

	public void run() {
	}

	public void start() {
		if (this.applet != null) {
			this.applet.start();
			return;
		}
		
		//Game Launch
		Thread t = new Thread() {
			public void run()
			{
				Launcher.this.updater.run();
				Launcher.this.launcher.run();
			}
		};
		t.setDaemon(true);
		t.start();
		
		//Launcher Graphic Update
		t = new Thread() {
			public void run()
			{
				while (Launcher.this.applet == null) {
					Launcher.this.repaint();
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	public void stop() {
		if (this.applet != null) {
			this.active = false;
			this.applet.stop();
			return;
		}
	}

	public void destroy() {
		if (this.applet != null) {
			this.applet.destroy();
			return;
		}
	}

	public void replace(Applet applet) {
		this.applet = applet;
		applet.setStub(this);
		applet.setSize(getWidth(), getHeight());

		setLayout(new BorderLayout());
		add(applet, "Center");

		applet.init();
		this.active = true;
		applet.start();
		validate();
		
		launcherFrame.setTitle(launcherFrame.config.getString("gameLauncher.gameName"));
	}

	public void paint(Graphics g2) {
		if (this.applet != null)
			return;

		int w = getWidth() / 2;
		int h = getHeight() / 2;
		if ((this.img == null) || (this.img.getWidth() != w)
				|| (this.img.getHeight() != h)) {
			this.img = createVolatileImage(w, h);
		}

		Graphics g = this.img.getGraphics();
		for (int x = 0; x <= w / 32; x++) {
			for (int y = 0; y <= h / 32; y++)
				g.drawImage(this.bgImage, x * 32, y * 32, null);
		}
		if (this.pauseAskUpdate) {
			if (!this.hasMouseListener) {
				this.hasMouseListener = true;
				addMouseListener(this);
			}
			g.setColor(Color.LIGHT_GRAY);
			String msg = launcherFrame.locale.getString("updater.newUpdateAvailable");
			g.setFont(new Font(null, 1, 20));
			FontMetrics fm = g.getFontMetrics();
			g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
					h / 2 - fm.getHeight() * 2);

			g.setFont(new Font(null, 0, 12));
			fm = g.getFontMetrics();

			g.fill3DRect(w / 2 - 56 - 8, h / 2, 56, 20, true);
			g.fill3DRect(w / 2 + 8, h / 2, 56, 20, true);

			msg = launcherFrame.locale.getString("updater.askUpdate");
			g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - 8);

			g.setColor(Color.BLACK);
			msg = launcherFrame.locale.getString("global.yes");
			g.drawString(msg, w / 2 - 56 - 8 - fm.stringWidth(msg) / 2 + 28,
					h / 2 + 14);
			msg = launcherFrame.locale.getString("global.no");
			g.drawString(msg, w / 2 + 8 - fm.stringWidth(msg) / 2 + 28,
					h / 2 + 14);
		} else {
			g.setColor(Color.LIGHT_GRAY);

			String msg = launcherFrame.locale.getString("updater.title");
			if (this.updater.fatalError) {
				msg = "Failed to launch";
			}

			g.setFont(new Font(null, 1, 20));
			FontMetrics fm = g.getFontMetrics();
			g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
					h / 2 - fm.getHeight() * 2);

			g.setFont(new Font(null, 0, 12));
			fm = g.getFontMetrics();
			msg = this.getDescriptionForState();
			if (this.updater.fatalError) {
				msg = this.updater.fatalErrorDescription;
			}

			g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
					h / 2 + fm.getHeight() * 1);
			msg = this.subtaskMessage;
			g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2,
					h / 2 + fm.getHeight() * 2);

			if (!this.updater.fatalError) {
				g.setColor(Color.black);
				g.fillRect(64, h - 64, w - 128 + 1, 5);
				g.setColor(new Color(32768));
				g.fillRect(64, h - 64, this.percentage * (w - 128)
						/ 100, 4);
				g.setColor(new Color(2138144));
				g.fillRect(65, h - 64 + 1, this.percentage
						* (w - 128) / 100 - 2, 1);
			}
		}

		g.dispose();

		g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
	}

	private String getDescriptionForState() {
		if(this.state.getDescription() != null)
			return this.state.getDescription();
		else
			return launcherFrame.locale.getString("updater.states." + this.state.name());
	}

	public void update(Graphics g) {
		paint(g);
	}

	private boolean contains(int x, int y, int xx, int yy, int w, int h) {
		return (x >= xx) && (y >= yy) && (x < xx + w) && (y < yy + h);
	}
	
	public String getParameter(String name) {
		String custom = (String) this.customParameters.get(name);
		if (custom != null)
			return custom;
		
		custom = this.launcherFrame.config.getString(name);
		if (custom != null)
			return custom;
		
		try {
			return super.getParameter(name);
		} catch (Exception e) {
			this.customParameters.put(name, null);
		}
		return null;
	}
	
	public URL getDocumentBase() {
		try {
			return new URL(launcherFrame.config.getString("gameLauncher.documentBaseURL"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isActive() {
		if (this.context == 0) {
			this.context = -1;
			try {
				if (getAppletContext() != null)
					this.context = 1;
			} catch (Exception localException) {
			}
		}
		if (this.context == -1)
			return this.active;
		return super.isActive();
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public int getPercentage() {
		return percentage;
	}
	
	public void setState(int state)
	{
		this.state = State.values()[state - 1];
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

}
