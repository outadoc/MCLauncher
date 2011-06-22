package com.kokakiwi.mclauncher.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JTextPane;

import com.kokakiwi.mclauncher.LauncherFrame;

public class ClassesUtils {

	public static class LaunchActionListener implements ActionListener {
		private LauncherFrame launcherFrame;
		
		public LaunchActionListener(LauncherFrame launcherFrame)
		{
			this.launcherFrame = launcherFrame;
		}

		public void actionPerformed(ActionEvent paramActionEvent) {
			this.launcherFrame.doLogin();
		}

	}

	public static class BrowserThread extends Thread {
		private JTextPane editorPane;
		public String url;
		
		public BrowserThread(JTextPane editorPane, String url)
		{
			this.editorPane = editorPane;
			this.url = url;
		}

		@Override
		public void run() {
			try {
				editorPane.setPage(new URL(this.url));
			} catch (Exception e) {
				editorPane.setText("<html><body>Error during loading page.</body></html>");
				e.printStackTrace();
			}
		}
	}
	
	public static class GameUpdaterThread extends Thread {
		public InputStream[] is;
		public URLConnection urlconnection;

		public void run() {
			try {
				this.is[0] = this.urlconnection.getInputStream();
			} catch (IOException localIOException) {
			}
		}
	}
	
}
