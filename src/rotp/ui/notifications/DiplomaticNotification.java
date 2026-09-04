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
package rotp.ui.notifications;

import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.model.incidents.DiplomaticIncident;
import rotp.ui.RotPUI;
import rotp.ui.diplomacy.DialogueManager;
import rotp.util.Base;

public class DiplomaticNotification implements TurnNotification, Base {
    private EmpireView view;
    private Empire talker;
    private Empire other;
    private String type;
    private DiplomaticIncident incident;
    private boolean returnToMap = false;
    private static int expansionWarningCount = 0;
    private static int bioweaponWarningCount = 0;
    private static int genocideWarningCount  = 0;

    public static void clearNotificationLimits() {
        expansionWarningCount = 0;
        bioweaponWarningCount = 0;
        genocideWarningCount  = 0;
    }
	public static void create(EmpireView v, String messageType)	{
		if (!notificationIsAllowed(messageType))
			return;
		DiplomaticNotification notif = new DiplomaticNotification(v, messageType);
		GameSession.addTurnNotification(notif);
	}
	public static void create(EmpireView v, String messageType, Empire otherEmp)	{
		if (!notificationIsAllowed(messageType))
			return;
		DiplomaticNotification notif = new DiplomaticNotification(v, messageType);
		notif.other = otherEmp;
		GameSession.addTurnNotification(notif);
	}
	public static void createAndNotify(EmpireView v, String messageType)	{
		if (!notificationIsAllowed(messageType))
			return;
		new DiplomaticNotification(v, messageType).notifyPlayer();
	}
	public static void create(EmpireView v, DiplomaticIncident inc, String messageType)	{
		if (!notificationIsAllowed(messageType))
			return;
		DiplomaticNotification notif = new DiplomaticNotification(v, inc, messageType);
		GameSession.addTurnNotification(notif);
	}
    public static DiplomaticNotification create(EmpireView v, DiplomaticIncident inc) {
        DiplomaticNotification notif = new DiplomaticNotification(v, inc);
        GameSession.addTurnNotification(notif);
        return notif;
    }
    public static DiplomaticNotification create(Empire talk, DiplomaticIncident inc) {
        DiplomaticNotification notif = new DiplomaticNotification(talk, inc);
        GameSession.addTurnNotification(notif);
        return notif;
    }
    public DiplomaticNotification() { }

    public DiplomaticNotification(EmpireView v, String messageType) {
        view = v;
        talker = v.owner();
        type = messageType;
    }
    protected DiplomaticNotification(EmpireView v, DiplomaticIncident inc, String messageType) {
        view = v;
        talker = v.owner();
        type = messageType;
        incident = inc;
    }
    protected DiplomaticNotification(EmpireView v, DiplomaticIncident inc) {
        view = v;
        talker = v.owner();
        type = inc.warningMessageId();
        incident = inc;
    }
    protected DiplomaticNotification(Empire talk, DiplomaticIncident inc) {
        view = null;
        talker = talk;
        type = inc.warningMessageId();
        incident = inc;
    }
    public Empire talker()               { return talker; }
    public Empire otherEmpire()          { return other; }
    public DiplomaticIncident incident() { return incident; }
    public String type()                 { return type; }
    public EmpireView view()             { return view; }
    public void setReturnToMap()         { returnToMap = true; }
    public boolean returnToMap()         { return returnToMap; }
    public void view(EmpireView v)       { view = v; talker = v.owner(); }
    @Override
    public String displayOrder() { return incident == null ? DIPLOMATIC_MESSAGE : incident.displayOrder(); }
	@Override  public void notifyPlayer()	{
		// BR: Test if this warning is allowed.
		// System.out.println("Diplomatic Notification: type = " + type);
		IGameOptions opts = GameSession.instance().options();
		switch (type) {
			case DialogueManager.WARNING_EXPANSION:
				expansionWarningCount += 1;
				if (opts.selectedMaxWarnings() < expansionWarningCount)
					return;
				break;
			case DialogueManager.WARNING_BIOWEAPON:
				bioweaponWarningCount += 1;
				if (opts.selectedMaxWarnings() < bioweaponWarningCount)
					return;
				break;
			case DialogueManager.WARNING_GENOCIDE:
				genocideWarningCount += 1;
				if (opts.selectedMaxWarnings() < genocideWarningCount)
					return;
				break;
		}
		RotPUI.instance().selectDiplomaticMessagePanel(this);
	}
	private static boolean notificationIsAllowed(String messageType)	{
		IGameOptions opts = GameSession.instance().options();
		switch (messageType) {
			case DialogueManager.WARNING_EXPANSION:	return opts.allowWarningExpansion();
			case DialogueManager.WARNING_BIOWEAPON:	return opts.allowWarningBioweapon();
			case DialogueManager.WARNING_GENOCIDE:	return opts.allowWarningGenocide();
		}
		return true;
	}
}
