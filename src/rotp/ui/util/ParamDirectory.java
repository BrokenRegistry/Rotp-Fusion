package rotp.ui.util;

import static rotp.ui.util.IParam.langLabel;

import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JFileChooser;

import rotp.Rotp;
import rotp.ui.game.BaseModPanel;

public class ParamDirectory extends ParamString	{
	public ParamDirectory(String gui, String name)	{
		super(gui, name, Rotp.jarPath());
		isCfgFile(true);
	}
	@Override protected String descriptionId()	{
		String es = get().isEmpty()? "1" : "2";
		String label = super.descriptionId() + es;
		return label;			
	}
	@Override public String getGuiDescription()	{ return langLabel(descriptionId(), get()); }
	@Override public boolean toggle(MouseEvent e, BaseModPanel frame)	{
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
		File file = new File(dir);
		if (!file.exists() || !file.isDirectory()) {
			dir = Rotp.jarPath();
			set(dir);
		}
		return dir;
	}
}