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
package rotp.ui.diplomacy;

public class TurnNotificationMessage extends DiplomaticMessage {
    protected TurnNotificationMessage() { }
    public TurnNotificationMessage(String  s) {
        messageType = s;
    }
	/**
	 * Blocking a player behind a bugged UI is not very useful
	 * Let them continue by pressing "Escape",
	 * The issue will be logged,
	 * and the testing developer will see this warning instead.
	 */
	@Override public void escape()	{
		quietlyValidateHotSeatCanAct();
		session().resumeNextTurnProcessing();
	}
	@Override public void select(int i)	{ escape(); }
}
