package com.kokakiwi.mclauncher.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.utils.ClassesUtils;
import com.kokakiwi.mclauncher.utils.State;
import com.kokakiwi.mclauncher.utils.Utils;

public class GameUpdater implements Runnable {
	private LauncherFrame launcherFrame;
	private Launcher launcher;

	private boolean lzmaSupported;
	private boolean pack200Supported;
	public URL[] urlList;
	public boolean fatalError = false;
	public String fatalErrorDescription;
	public boolean shouldUpdate = false;
	private int totalSizeDownload;
	private int currentSizeDownload;
	private int totalSizeExtract;
	private int currentSizeExtract;

	public GameUpdater(LauncherFrame launcherFrame) {
		this.launcherFrame = launcherFrame;
		this.launcher = launcherFrame.launcher;
	}

	public void run() {
		init();
		launcher.setPercentage(5);
		
		try {
			loadJarURLs();
			
			String path = (String) AccessController
					.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
							return Utils.getWorkingDirectory(launcherFrame)
									+ File.separator + "bin" + File.separator;
						}
					});

			File dir = new File(path);
			
			if(!dir.exists())
				dir.mkdirs();
			
			String latestVersion = launcherFrame.config.getString("latestVersion");
			
			if(latestVersion != null)
			{
				boolean forceUpdate = launcherFrame.config.getString("force-update") == null ? false : true;
				File versionFile = new File(dir, "version");
				
				boolean cacheAvailable = false;
				if ((!forceUpdate) && (versionFile.exists()) && ((latestVersion.equals("-1")) || (latestVersion.equals(readVersionFile(versionFile))))) {
					cacheAvailable = true;
					launcher.setPercentage(90);
				}
				
				if ((forceUpdate) || (!cacheAvailable)) {
					this.shouldUpdate = true;
					if ((!forceUpdate) && (versionFile.exists())) {
						checkShouldUpdate();
					}
					if (this.shouldUpdate || forceUpdate) {
						writeVersionFile(versionFile, "");

						downloadJars(path);
						extractJars(path);
						extractNatives(path);

						if (latestVersion != null && !((latestVersion.equals("-1")))) {
							launcher.setPercentage(90);
							writeVersionFile(versionFile, latestVersion);
						}
					} else {
						cacheAvailable = true;
						launcher.setPercentage(90);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		launcher.setPercentage(90);
	}
	
	protected void loadJarURLs() throws Exception {
		launcher.setState(State.DETERMINING_PACKAGE);
		List<String> jarList = launcherFrame.config.getStringList("updater.jarList");
		this.urlList = new URL[jarList.size() + 1];
		
		for(int i = 0; i < jarList.size(); i++)
		{
			this.urlList[i] = new URL(jarList.get(i));
		}

		Utils.OS osName = Utils.getPlatform();
		String nativeJar = null;
		
		if(osName == Utils.OS.unknown)
			fatalErrorOccured("OS (" + System.getProperty("os.name") + ") not supported", null);
		else
			nativeJar = launcherFrame.config.getString("updater.nativesList." + osName.name());

		if (nativeJar == null) {
			fatalErrorOccured("no lwjgl natives files found", null);
		} else {
			nativeJar = trimExtensionByCapabilities(nativeJar);
			this.urlList[jarList.size()] = new URL(nativeJar);
		}
	}

	private void checkShouldUpdate() {
		launcher.pauseAskUpdate = true;
		while (launcher.pauseAskUpdate)
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	protected void downloadJars(String path) throws Exception {
		File versionFile = new File(path, "md5s");
		Properties md5s = new Properties();
		boolean forceUpdate = launcherFrame.config.getString("force-update") == null ? false : true;
		if (versionFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(versionFile);
				md5s.load(fis);
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		launcher.setState(State.DOWNLOADING);

		int[] fileSizes = new int[this.urlList.length];
		boolean[] skip = new boolean[this.urlList.length];

		for (int i = 0; i < this.urlList.length; i++) {
			URLConnection urlconnection = this.urlList[i].openConnection();
			urlconnection.setDefaultUseCaches(false);
			skip[i] = false;
			if ((urlconnection instanceof HttpURLConnection)) {
				((HttpURLConnection) urlconnection).setRequestMethod("HEAD");

				String etagOnDisk = "\""
						+ md5s.getProperty(getFileName(this.urlList[i])) + "\"";

				if ((!forceUpdate) && (etagOnDisk != null))
					urlconnection.setRequestProperty("If-None-Match",
							etagOnDisk);

				int code = ((HttpURLConnection) urlconnection)
						.getResponseCode();
				if (code / 100 == 3) {
					skip[i] = true;
				}
			}
			fileSizes[i] = urlconnection.getContentLength();
			this.totalSizeDownload += fileSizes[i];
		}

		int initialPercentage = 10;
		launcher.setPercentage(initialPercentage);

		byte[] buffer = new byte[65536];
		for (int i = 0; i < this.urlList.length; i++) {
			if (skip[i]) {
				launcher.setPercentage(initialPercentage + fileSizes[i] * 45 / this.totalSizeDownload);
			} else {
				try {
					md5s.remove(getFileName(this.urlList[i]));
					md5s.store(new FileOutputStream(versionFile),
							"md5 hashes for downloaded files");
				} catch (Exception e) {
					e.printStackTrace();
				}

				int unsuccessfulAttempts = 0;
				int maxUnsuccessfulAttempts = 3;
				boolean downloadFile = true;

				while (downloadFile) {
					downloadFile = false;

					URLConnection urlconnection = this.urlList[i]
							.openConnection();

					String etag = "";

					if ((urlconnection instanceof HttpURLConnection)) {
						urlconnection.setRequestProperty("Cache-Control",
								"no-cache");

						urlconnection.connect();

						etag = urlconnection.getHeaderField("ETag");
						etag = etag.substring(1, etag.length() - 1);
					}

					String currentFile = getFileName(this.urlList[i]);
					InputStream inputstream = getJarInputStream(currentFile,
							urlconnection);
					FileOutputStream fos = new FileOutputStream(path
							+ currentFile);

					long downloadStartTime = System.currentTimeMillis();
					int downloadedAmount = 0;
					int fileSize = 0;
					String downloadSpeedMessage = "";

					MessageDigest m = MessageDigest.getInstance("MD5");
					int bufferSize;
					while ((bufferSize = inputstream.read(buffer, 0,
							buffer.length)) != -1) {
						fos.write(buffer, 0, bufferSize);
						m.update(buffer, 0, bufferSize);
						this.currentSizeDownload += bufferSize;
						fileSize += bufferSize;
						launcher.setPercentage(initialPercentage + this.currentSizeDownload * 45 / this.totalSizeDownload);
						launcher.subtaskMessage = ("Retrieving: " + currentFile
								+ " " + this.currentSizeDownload * 100
								/ this.totalSizeDownload + "%");

						downloadedAmount += bufferSize;
						long timeLapse = System.currentTimeMillis()
								- downloadStartTime;

						if (timeLapse >= 1000L) {
							float downloadSpeed = downloadedAmount
									/ (float) timeLapse;
							downloadSpeed = (int) (downloadSpeed * 100.0F) / 100.0F;
							downloadSpeedMessage = " @ " + downloadSpeed
									+ " KB/sec";
							downloadedAmount = 0;
							downloadStartTime += 1000L;
						}

						launcher.subtaskMessage += downloadSpeedMessage;
					}

					inputstream.close();
					fos.close();
					String md5 = new BigInteger(1, m.digest()).toString(16);
					while (md5.length() < 32) {
						md5 = "0" + md5;
					}
					boolean md5Matches = true;
					if (etag != null) {
						md5Matches = md5.equals(etag);
					}

					if ((urlconnection instanceof HttpURLConnection)) {
						if ((md5Matches)
								&& ((fileSize == fileSizes[i]) || (fileSizes[i] <= 0))) {
							try {
								md5s.setProperty(getFileName(this.urlList[i]),
										etag);
								md5s.store(new FileOutputStream(versionFile),
										"md5 hashes for downloaded files");
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							unsuccessfulAttempts++;
							if (unsuccessfulAttempts < maxUnsuccessfulAttempts) {
								downloadFile = true;
								this.currentSizeDownload -= fileSize;
							} else {
								throw new Exception("failed to download "
										+ currentFile);
							}
						}
					}
				}
			}
		}

		launcher.subtaskMessage = "";
	}

	protected void extractJars(String path) throws Exception {
		launcher.setState(State.EXTRACTING_PACKAGES);

		float increment = 10.0F / this.urlList.length;

		for (int i = 0; i < this.urlList.length; i++) {
			launcher.setPercentage(55 + (int) (increment * (i + 1)));
			String filename = getFileName(this.urlList[i]);

			if (filename.endsWith(".pack.lzma")) {
				launcher.subtaskMessage = ("Extracting: " + filename + " to " + filename
						.replaceAll(".lzma", ""));
				extractLZMA(path + filename,
						path + filename.replaceAll(".lzma", ""));

				launcher.subtaskMessage = ("Extracting: "
						+ filename.replaceAll(".lzma", "") + " to " + filename
						.replaceAll(".pack.lzma", ""));
				extractPack(path + filename.replaceAll(".lzma", ""), path
						+ filename.replaceAll(".pack.lzma", ""));
			} else if (filename.endsWith(".pack")) {
				launcher.subtaskMessage = ("Extracting: " + filename + " to " + filename
						.replace(".pack", ""));
				extractPack(path + filename,
						path + filename.replace(".pack", ""));
			} else if (filename.endsWith(".lzma")) {
				launcher.subtaskMessage = ("Extracting: " + filename + " to " + filename
						.replace(".lzma", ""));
				extractLZMA(path + filename,
						path + filename.replace(".lzma", ""));
			}
		}
	}

	protected void extractNatives(String path) throws Exception {
		launcher.setState(State.EXTRACTING_PACKAGES);

		int initialPercentage = launcher.getPercentage();

		String nativeJar = getJarName(this.urlList[(this.urlList.length - 1)]);
		File nativeFolder = new File(path + "natives");
		if (!nativeFolder.exists()) {
			nativeFolder.mkdir();
		}

		File file = new File(path + nativeJar);
		if (!file.exists())
			return;
		JarFile jarFile = new JarFile(file, true);
		Enumeration<JarEntry> entities = jarFile.entries();

		this.totalSizeExtract = 0;

		while (entities.hasMoreElements()) {
			JarEntry entry = (JarEntry) entities.nextElement();

			if ((entry.isDirectory()) || (entry.getName().indexOf('/') != -1)) {
				continue;
			}
			this.totalSizeExtract = (int) (this.totalSizeExtract + entry
					.getSize());
		}

		this.currentSizeExtract = 0;

		entities = jarFile.entries();

		while (entities.hasMoreElements()) {
			JarEntry entry = (JarEntry) entities.nextElement();

			if ((entry.isDirectory()) || (entry.getName().indexOf('/') != -1)) {
				continue;
			}
			File f = new File(path + "natives" + File.separator
					+ entry.getName());
			if ((f.exists()) && (!f.delete())) {
				continue;
			}

			InputStream in = jarFile.getInputStream(jarFile.getEntry(entry
					.getName()));
			OutputStream out = new FileOutputStream(path + "natives"
					+ File.separator + entry.getName());

			byte[] buffer = new byte[65536];
			int bufferSize;
			while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, bufferSize);
				this.currentSizeExtract += bufferSize;

				launcher.setPercentage(initialPercentage + this.currentSizeExtract
						* 20 / this.totalSizeExtract);
				launcher.subtaskMessage = ("Extracting: " + entry.getName() + " "
						+ this.currentSizeExtract * 100 / this.totalSizeExtract + "%");
			}

			in.close();
			out.close();
		}
		launcher.subtaskMessage = "";

		jarFile.close();

		File f = new File(path + nativeJar);
		f.delete();
	}
	
	protected String trimExtensionByCapabilities(String file) {
		if (!this.pack200Supported) {
			file = file.replaceAll(".pack", "");
		}

		if (!this.lzmaSupported) {
			file = file.replaceAll(".lzma", "");
		}
		return file;
	}
	
	protected String getJarName(URL url) {
		String fileName = url.getFile();

		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf("?"));
		}
		if (fileName.endsWith(".pack.lzma"))
			fileName = fileName.replaceAll(".pack.lzma", "");
		else if (fileName.endsWith(".pack"))
			fileName = fileName.replaceAll(".pack", "");
		else if (fileName.endsWith(".lzma")) {
			fileName = fileName.replaceAll(".lzma", "");
		}

		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}
	
	protected void fatalErrorOccured(String error, Exception e) {
		e.printStackTrace();
		this.fatalError = true;
		this.fatalErrorDescription = ("Fatal error occured (" + launcher.getState()
				+ "): " + error);
	}

	protected String readVersionFile(File file) throws Exception {
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		String version = dis.readUTF();
		dis.close();
		return version;
	}

	protected void writeVersionFile(File file, String version) throws Exception {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		dos.writeUTF(version);
		dos.close();
	}
	
	protected String getFileName(URL url) {
		String fileName = url.getFile();
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf("?"));
		}
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}
	
	protected InputStream getJarInputStream(String currentFile,
			URLConnection urlconnection) throws Exception {
		InputStream[] is = new InputStream[1];

		for (int j = 0; (j < 3) && (is[0] == null); j++) {
			ClassesUtils.GameUpdaterThread t = new ClassesUtils.GameUpdaterThread();
			t.is = is;
			t.urlconnection = urlconnection;
			t.setName("JarInputStreamThread");
			t.start();

			int iterationCount = 0;
			while ((is[0] == null) && (iterationCount++ < 5)) {
				try {
					t.join(1000L);
				} catch (InterruptedException localInterruptedException) {
				}
			}
			if (is[0] != null)
				continue;
			try {
				t.interrupt();
				t.join();
			} catch (InterruptedException localInterruptedException1) {
			}
		}

		if (is[0] == null) {
			throw new Exception("Unable to download " + currentFile);
		}

		return is[0];
	}
	
	protected void extractLZMA(String in, String out) throws Exception {
		File f = new File(in);
		if (!f.exists())
			return;
		FileInputStream fileInputHandle = new FileInputStream(f);

		Class<?> clazz = Class.forName("LZMA.LzmaInputStream");
		Constructor<?> constructor = clazz
				.getDeclaredConstructor(new Class[] { InputStream.class });

		InputStream inputHandle = (InputStream) constructor
				.newInstance(new Object[] { fileInputHandle });

		OutputStream outputHandle = new FileOutputStream(out);

		byte[] buffer = new byte[16384];

		int ret = inputHandle.read(buffer);
		while (ret >= 1) {
			outputHandle.write(buffer, 0, ret);
			ret = inputHandle.read(buffer);
		}

		inputHandle.close();
		outputHandle.close();

		outputHandle = null;
		inputHandle = null;

		f.delete();
	}

	protected void extractPack(String in, String out) throws Exception {
		File f = new File(in);
		if (!f.exists())
			return;

		FileOutputStream fostream = new FileOutputStream(out);
		JarOutputStream jostream = new JarOutputStream(fostream);

		Pack200.Unpacker unpacker = Pack200.newUnpacker();
		unpacker.unpack(f, jostream);
		jostream.close();

		f.delete();
	}
	
	public void init()
	{
		try {
			Class.forName("LZMA.LzmaInputStream");
			this.lzmaSupported = true;
		} catch (Throwable localThrowable) {
		}
		try {
			Pack200.class.getSimpleName();
			this.pack200Supported = true;
		} catch (Throwable localThrowable1) {
		}
	}

}
