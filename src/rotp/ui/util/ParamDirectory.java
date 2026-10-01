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
			return copyFromRessource(template);
		return false;
	}
	public boolean copyFromRessource(String template)	{
		URL url = Rotp.class.getResource(template);
		File dest = new File(get());
		return CopyUtils.copyResourcesRecursively(url, dest);
	}

	public class CopyUtils {
		public static boolean copyResourcesRecursively(final URL srcUrl, final File destDir)	{
			try {
				URLConnection urlConnection = srcUrl.openConnection();
				if (urlConnection instanceof JarURLConnection)
					return copyJarResourcesRecursively((JarURLConnection) urlConnection, destDir);
				else {
					File src = new File(srcUrl.getPath());
					if (destDir.exists() || destDir.mkdir())
						return copyFolderContent(src, destDir);
					else
						return false;
				}
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		private static boolean copyJarResourcesRecursively(JarURLConnection jarSrc, File destDir) throws IOException	{
			final JarFile jarFile = jarSrc.getJarFile();

			for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
				JarEntry entry = e.nextElement();
				if (entry.getName().startsWith(jarSrc.getEntryName())) {
					String filename = Strings.CS.removeStart(entry.getName(), jarSrc.getEntryName());
					File f = new File(destDir, filename);
					if (!entry.isDirectory()) {
						InputStream entryInputStream = jarFile.getInputStream(entry);
						if(!copyStream(entryInputStream, f)) {
							entryInputStream.close();
							return false;
						}
						entryInputStream.close();
					} 
					else {
						if (!ensureDirectoryExists(f)) {
							jarFile.close();
							throw new IOException("Could not create directory: " + f.getAbsolutePath());
						}
					}
				}
			}
			jarFile.close();
			return true;
		}
		private static boolean ensureDirectoryExists(File f)		{ return f.exists() || f.mkdir(); }
		private static boolean copyStream(InputStream is, File f)	{
			try {
				FileOutputStream out = new FileOutputStream(f);
				boolean copied = copyStream(is, out);
				try { out.close(); }
				catch (IOException e) { e.printStackTrace(); }
				return copied;
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return false;
		}

		private static boolean copyFolderContent(File srcDir, File destDir)	{
			assert srcDir.isDirectory();
			assert destDir.isDirectory();
			for (File child : srcDir.listFiles())
				if (child.isDirectory()) {
					File subDir = new File(destDir, child.getName());
					if (subDir.exists() || subDir.mkdir())
						copyFolderContent(child, subDir);
					else
						return false; // Don't exist and can't create it
				}
				else 
					copyFile(child, new File(destDir, child.getName()));
			return true;
		}
		private static boolean copyFile(File toCopy, File destFile) {
			try {
				FileInputStream src = new FileInputStream(toCopy);
				FileOutputStream dest = new FileOutputStream(destFile);
				boolean copied = copyStream(src, dest);
				try { dest.close(); }
				catch (IOException e) { e.printStackTrace(); }
				try { src.close(); }
				catch (IOException e) { e.printStackTrace(); }
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
				while ((len = is.read(buf)) > 0)
					os.write(buf, 0, len);

				is.close();
				os.close();
				return true;
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}