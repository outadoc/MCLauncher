package com.kokakiwi.mclauncher.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.MCLauncher;

public class Utils {
	public static File workDir = null;
	
	public static InputStream getResourceAsStream(String url)
	{
		return LauncherFrame.class.getResourceAsStream("/" + url);
	}
	
	public static File getWorkingDirectory(LauncherFrame launcherFrame)
	{
		return getWorkingDirectory(launcherFrame.config.getString("updater.folderName"), (launcherFrame.config.getBoolean("updater.customGameDir") ? launcherFrame.config.getString("updater.gameDir") : null));
	}
	
	public static File getWorkingDirectory(String applicationName, String local) {
		if(workDir != null)
			return workDir;
		
		String userHome = System.getProperty("user.home", ".");
		File workingDirectory;
		switch (Utils.OS.values()[getPlatform().ordinal()]) {
		case linux:
		case solaris:
			workingDirectory = new File(userHome, '.' + applicationName + '/');
			break;
		case windows:
			String applicationData = System.getenv("APPDATA");
			if (applicationData != null)
				workingDirectory = new File(applicationData, "." + applicationName + '/');
			else
				workingDirectory = new File(userHome, '.' + applicationName + '/');
			break;
		case macos:
			workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
			break;
		default:
			workingDirectory = new File(userHome, applicationName + '/');
		}
		
		if(local != null)
		{
			workingDirectory = new File(new File(local + "/").getAbsoluteFile(), "." + applicationName + "/");
		}
		
		if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs()))
			throw new RuntimeException(
					"The working directory could not be created: "
							+ workingDirectory);
		workDir = workingDirectory;
		
		return workDir;
	}
	
	public static String executePost(String targetURL, String urlParameters, String keyFileName) {
		String protocol = targetURL.substring(4);
		HttpURLConnection connection = null;
		try {
			URL url = new URL(targetURL);
			if(protocol.contains("https"))
			{
				connection = (HttpsURLConnection) url.openConnection();
			}else {
				connection = (HttpURLConnection) url.openConnection();
			}
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length",
					Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			connection.connect();
			
			if(protocol.contains("https"))
			{
				Certificate[] certs = ((HttpsURLConnection) connection).getServerCertificates();

				byte[] bytes = new byte[294];
				DataInputStream dis = new DataInputStream(getResourceAsStream("keys/" + keyFileName));
				dis.readFully(bytes);
				dis.close();

				Certificate c = certs[0];
				PublicKey pk = c.getPublicKey();
				byte[] data = pk.getEncoded();

				for (int i = 0; i < data.length; i++) {
					if (data[i] == bytes[i])
						continue;
					throw new RuntimeException("Public key mismatch");
				}
			}

			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			StringBuffer response = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			String str1 = response.toString();
			return str1;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}
	
	public static void openLink(URI uri) {
		try {
			Object o = Class.forName("java.awt.Desktop")
					.getMethod("getDesktop", new Class[0])
					.invoke(null, new Object[0]);
			o.getClass().getMethod("browse", new Class[] { URI.class })
					.invoke(o, new Object[] { uri });
		} catch (Throwable e) {
			System.out.println("Failed to open link " + uri.toString());
		}
	}
	
	public static OS getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win"))
			return OS.windows;
		if (osName.contains("mac"))
			return OS.macos;
		if (osName.contains("solaris"))
			return OS.solaris;
		if (osName.contains("sunos"))
			return OS.solaris;
		if (osName.contains("linux"))
			return OS.linux;
		if (osName.contains("unix"))
			return OS.linux;
		return OS.unknown;
	}
	
	public static enum OS {
		linux, solaris, windows, macos, unknown;
	}
}
