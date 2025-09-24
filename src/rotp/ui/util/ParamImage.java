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

import java.awt.image.BufferedImage;

import rotp.model.game.IGameOptions;

public class ParamImage extends AbstractParam<BufferedImage> {
	
	// ===== Constructors =====
	//
	public ParamImage(String gui, String title) {
		super(gui, title, null);
	}

	// ===== Overriders =====
	//
	@Override public void updateOptionTool()	{}
	@Override public boolean isImage()			{ return true; }
	@Override public boolean isDefaultValue()	{ return true; }
	@Override public String getGuide()			{ return headerHelp(false); }
	@Override public float heightFactor()		{ return 20f; } // TODO BR: continue
	@Override protected BufferedImage getOptionValue(IGameOptions options)	{ return null; }
	@Override protected void setOptionValue(IGameOptions options, BufferedImage value)	{}

}
