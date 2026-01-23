/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rotp.ui.util;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;

public class ParamString extends AbstractParam<String> {
	
	private String inputMessage = "Enter the new name";
	
	// ===== Constructors =====
	//
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 */
	public ParamString(String gui, String name, String defaultValue) {
		super(gui, name, defaultValue);
	}
	// ===== Overriders =====
	//
	@Override public ParamString isValueInit(boolean is) { super.isValueInit(is) ; return this; }
	@Override public ParamString isDuplicate(boolean is) { super.isDuplicate(is) ; return this; }
	@Override public ParamString isCfgFile(boolean is)	 { super.isCfgFile(is)   ; return this; }
	@Override public ParamString setUpdateParameters(IUpdated method, String id)	{
		super.setUpdateParameters(method, id);
		return this;
	}

	@Override public void setFromCfgValue(String newValue)	{ setFromCfg(newValue); }	
	@Override public boolean prev() { return false; }
	@Override public boolean next() { return false; }
	@Override public boolean toggle(MouseWheelEvent e)	{ return false; }
	@Override protected String getOptionValue(IGameOptions options) {
		String value = options.dynOpts().getString(getLangLabel());
		if (value == null)
			if (formerName() == null)
				value = creationValue();
			else
				value = options.dynOpts().getString(formerName(), creationValue());
		return value;
	}
	@Override protected void setOptionValue(IGameOptions options, String value) {
		options.dynOpts().setString(getLangLabel(), value);
	}
	@Override public boolean toggle(MouseEvent e, BasePanel frame)	{
		Object prev = UIManager.get("OptionPane.minimumSize");
		UIManager.put("OptionPane.minimumSize", new Dimension(RotPUI.scaledSize(200),RotPUI.scaledSize(90))); 
		String input;
		input = JOptionPane.showInputDialog(frame ,inputMessage, get());
		UIManager.put("OptionPane.minimumSize", prev); 
		if (input == null)
			return false; // cancelled
		set(input);
		return false;
	}
	@Override public LinkValue linkValue(String val)	{ return new LinkValue(val); } 
	@Override protected String linkValue(LinkValue val)	{ return val.stringValue(); }
}
