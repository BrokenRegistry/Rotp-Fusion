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

import java.util.List;
import rotp.model.empires.Empire;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.ui.RotPUI;

public class GNNNotification implements TurnNotification {
    private final String message;
    private final String eventId;
    private final boolean allianceType;
    private final boolean nonEssentialType;


    public static void notifyAllianceFormed(String message) {
        GameSession.addTurnNotification(new GNNNotification(message, "GNN_Alliance_Formed", true, true));
    }
    public static void notifyAllianceBroken(String message) {
        GameSession.addTurnNotification(new GNNNotification(message, "GNN_Alliance_Broken", true, true));
    }
    public static void notifyCouncil(String message) {
        GameSession.addTurnNotification(new GNNNotification(message, "GNN_Expansion", false, false));
    }
    public static void notifyExpansion(String message) {
        GameSession.addTurnNotification(new GNNNotification(message, "GNN_Expansion", false, true));
    }
    public static void notifyRebellion(String message, boolean player) {
        GameSession.addTurnNotification(new GNNNotification(message, "GNN_Rebellion", false, !player));
    }
    public static void notifyGenocide(String message) {
        GameSession.addTurnNotification(new GNNNotification(message, "GNN_Genocide", false, true));
    }
    public static void notifyImmediateEvent(String message, String id) {
        RotPUI.instance().processNotification(new GNNRandomEventNotification(message, id));
    }
    public static void notifyRandomEvent(String message, String id) {
        GameSession.addTurnNotification(new GNNRandomEventNotification(message, id));
    }
    public static void notifyRanking(String message, List<Empire> empireList) {
        GameSession.addTurnNotification(new GNNRankingNotification(message, empireList, "GNN_Ranking"));
    }
    private GNNNotification(String msg, String id, boolean alliance, boolean nonEssential) {
        message = msg;
        eventId = id;
        allianceType = alliance;
        nonEssentialType = nonEssential;
    }
    @Override
    public String displayOrder() { return GNN_NOTIFY; }
    @Override
    public void notifyPlayer() {
    	// BR: Test if this announcement is allowed.
    	IGameOptions opts = GameSession.instance().options();
    	if (allianceType && opts.hideAlliancesGNN())
    		return;
    	if (nonEssentialType && opts.hideMinorReports())
    		return;
//    	switch (eventId) {
//			case "GNN_Alliance_Formed":
//			case "GNN_Alliance_Broken":
//				if (opts.hideAlliancesGNN())
//					return;
//			case "GNN_Expansion":
//				
//    	}
        RotPUI.instance().selectGNNPanel(message, eventId, null);
    }
    @Override public String toString() { return "Event: " + eventId + ": " + message; }
    
}
