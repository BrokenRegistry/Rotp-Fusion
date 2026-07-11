package rotp.ui.util;

import static rotp.ui.util.IParam.langLabel;

import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Paths;

import javax.swing.JFileChooser;

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
}