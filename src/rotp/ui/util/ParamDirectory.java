package rotp.ui.util;

import static rotp.ui.util.IParam.langLabel;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFileChooser;

import org.apache.commons.lang3.Strings;

import rotp.Rotp;
import rotp.ui.BasePanel;

public class ParamDirectory extends ParamString	{
	public ParamDirectory(String gui, String name)	{
		super(gui, name, Rotp.jarPath());
		isCfgFile(true);
	}
	public ParamDirectory(String gui, String name, String folderName)	{
		super(gui, name, Paths.get(Rotp.jarPath(), folderName).toString());
		isCfgFile(true);
	}
	@Override protected String descriptionId()	{
		String es = get().isEmpty()? "1" : "2";
		String label = super.descriptionId() + es;
		return label;			
	}
	@Override public String getGuiDescription()	{ return langLabel(descriptionId(), get()); }
	@Override public boolean toggle(MouseEvent e, BasePanel frame)	{
		if (getDir(e) == 0) {
			set(defaultValue());
			return false;
		}
		final JFileChooser fc = new RotpFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File saveDir = new File(get());
		fc.setCurrentDirectory(saveDir);
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getAbsolutePath();
			set(path);
		}
		return false;
	}
	@Override public String get()	{ // Always return a valid directory
		String dir = super.get();
		if (dir == null) {
			dir = Rotp.jarPath();
			set(dir);
		}
		else {
			File file = new File(dir);
			if (!file.exists() || !file.isDirectory()) {
				dir = Rotp.jarPath();
				set(dir);
			}
		}
		return dir;
	}
	public boolean isJarPath() { return Rotp.jarPath().equalsIgnoreCase(get()); }
	public boolean createNewDefault(String folderName) {
		File newFolder = new File(Rotp.jarPath(), folderName);
		if (newFolder.exists()) {
			if (!newFolder.isDirectory())
				return false;
			set(newFolder.getPath());
			return true;
		}
		else if (!newFolder.mkdirs())
			return false;
		set(newFolder.getPath());
		return true;
	}
	public boolean createNewDefault(String folderName, String template)	{
		if (createNewDefault(folderName))
			return copyFromRessource(template, true);
		return false;
	}
	public boolean copyFromRessource(String template, boolean over)	{
		URL url = Rotp.class.getResource(template);
		File dest = new File(get());
		File parent = dest.getParentFile();
		if (over && parent != null)
			return FileUtils.copyResourcesRecursively(url, parent);
		else
			return FileUtils.copyResourcesRecursively(url, dest);
	}

	// Source - https://stackoverflow.com/a/3348150
	// Posted by Jabber, modified by community. See post 'Timeline' for change history
	// Retrieved 2026-09-27, License - CC BY-SA 3.0
	public class FileUtils {
		private static boolean copyFile(final File toCopy, final File destFile) {
			try {
				FileInputStream src = new FileInputStream(toCopy);
				FileOutputStream dest = new FileOutputStream(destFile);
				boolean copied = FileUtils.copyStream(src, dest);
				try { dest.close(); }
				catch (IOException e) {
					e.printStackTrace();
				}
				try { src.close(); }
				catch (IOException e) {
					e.printStackTrace();
				}
				return copied;
			}
			catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
			return false;
		}
		private static boolean copyFilesRecusively(final File toCopy, final File destDir) {
			assert destDir.isDirectory();

			if (!toCopy.isDirectory())
				return FileUtils.copyFile(toCopy, new File(destDir, toCopy.getName()));
			else {
				final File newDestDir = new File(destDir, toCopy.getName());
				if (!newDestDir.exists() && !newDestDir.mkdir())
					return false;

				for (final File child : toCopy.listFiles()) {
					if (!FileUtils.copyFilesRecusively(child, newDestDir)) {
						return false;
					}
				}
			}
			return true;
		}
		private static boolean copyJarResourcesRecursively(final File destDir, final JarURLConnection jarConnection) throws IOException {
			final JarFile jarFile = jarConnection.getJarFile();

			for (final Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
				final JarEntry entry = e.nextElement();
				if (entry.getName().startsWith(jarConnection.getEntryName())) {
					final String filename = Strings.CS.removeStart(entry.getName(), jarConnection.getEntryName());
					final File f = new File(destDir, filename);
					if (!entry.isDirectory()) {
						final InputStream entryInputStream = jarFile.getInputStream(entry);
						if(!FileUtils.copyStream(entryInputStream, f)) {
							entryInputStream.close();
							return false;
						}
						entryInputStream.close();
					} 
					else {
						if (!FileUtils.ensureDirectoryExists(f)) {
							jarFile.close();
							throw new IOException("Could not create directory: " + f.getAbsolutePath());
						}
					}
				}
			}
			jarFile.close();
			return true;
		}
		public static boolean copyResourcesRecursively(final URL originUrl, final File destination) {
			try {
				final URLConnection urlConnection = originUrl.openConnection();
				if (urlConnection instanceof JarURLConnection)
					return FileUtils.copyJarResourcesRecursively(destination, (JarURLConnection) urlConnection);
				else {
					return FileUtils.copyFilesRecusively(new File(originUrl.getPath()), destination);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		private static boolean copyStream(final InputStream is, final File f) {
			try {
				FileOutputStream out = new FileOutputStream(f);
				boolean copied =  FileUtils.copyStream(is, out);
				try { out.close(); }
				catch (IOException e) {
					e.printStackTrace();
				}
				return copied;
			}
			catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
			return false;
		}
		private static boolean copyStream(final InputStream is, final OutputStream os) {
			try {
				final byte[] buf = new byte[1024];

				int len = 0;
				while ((len = is.read(buf)) > 0) {
					os.write(buf, 0, len);
				}
				is.close();
				os.close();
				return true;
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		private static boolean ensureDirectoryExists(final File f) {
			return f.exists() || f.mkdir();
		}
	}
}