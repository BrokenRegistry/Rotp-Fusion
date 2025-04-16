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
package rotp.model.game;

import java.io.Serializable;

import rotp.util.Base;

public class GameStatus implements Base, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int wonNewRepublicAsAllied   = 5;
    private static final int wonNewRepublicAsChampion = 6;
    private static final int playerIsFeared = 0; // > 2/3 of total population
    private static final int playerIsMajor  = 1;
    private static final int playerIsMinor  = 2; // < 1/2 of total population
    public  static final int playerIsLeader        = 0; // Most populous
    public  static final int playerIsChallenger    = 1; // second most populous
    private  static final int playerIsNonCandidate = 2; // Neither Leader nor Challenger
    
    enum Status { NO_GAME, IN_PROGRESS, LOSS_OVERTHROWN, LOSS_MILITARY, LOSS_DIPLOMATIC, 
        LOSS_NEW_REPUBLIC, LOSS_REBELLION, WIN_DIPLOMATIC, WIN_MILITARY, WIN_MILITARY_ALLIANCE, 
        WIN_NEW_REPUBLIC, WIN_REBELLION, WIN_REBELLION_ALLIANCE, WIN_COUNCIL_ALLIANCE, LOSS_NO_COLONIES; }
    private Status status = Status.NO_GAME;
    // BR: changing Enum will break backward compatibility!
    private int subStatus      = 0;
    private int playerStatus   = 0;
    private int playerStrength = 0;
    private boolean allianceWithLeader = false;
    

    private boolean isMinor()        { return playerStrength == playerIsMinor; }
    private boolean isMajor()        { return playerStrength == playerIsMajor; }
    private boolean isFeared()       { return playerStrength == playerIsFeared; }
    private boolean isLeader()       { return playerStatus == playerIsLeader; } // Original leader
    private boolean isChallenger()   { return playerStatus == playerIsChallenger; } // Original Challenger
    private boolean isNonCandidate() { return playerStatus == playerIsNonCandidate; } // Not Candidate

    String key() { return status.toString(); }
    public boolean inProgress()    {
    	if (status != Status.NO_GAME && status != Status.IN_PROGRESS && IDebugOptions.debugAutoRun())
    		return GameSession.instance().galaxy().numActiveEmpires() > 1;
    	if (GameSession.instance().galaxy() != null
    			&& (GameSession.instance().aFewMoreTurns() || options().continueAnyway()))
    		return !GameSession.instance().galaxy().player().extinct();
    	return status == Status.IN_PROGRESS;
    }
    public boolean lost() {
    	return lostOverthrown() || lostMilitary() || lostDiplomatic()
    		|| lostNewRepublic() || lostRebellion() || lostNoColonies(); 
    }
    public boolean won() {
    	return wonCouncilAlliance() || wonMilitary() || wonMilitaryAlliance() 
            || wonNewRepublic() || wonRebellion() || wonRebellionAlliance() || wonDiplomatic();
    }
    public void allianceWithLeader(boolean b)      { allianceWithLeader = b; }
    public void playerStatus(int status)           { playerStatus = status; }
    public void playerVotesRatio(float ratio)      {
    	if (ratio < 0.5f)
    		playerStrength = playerIsMinor;
    	else if (ratio > 2/3f)
    		playerStrength = playerIsFeared;
    	else
    		playerStrength = playerIsMajor;
    }
    public boolean lostOverthrown()                { return status == Status.LOSS_OVERTHROWN; }
    public boolean lostMilitary()                  { return status == Status.LOSS_MILITARY; }
    public boolean lostNoColonies()                { return status == Status.LOSS_NO_COLONIES; }
    public boolean lostDiplomatic()                { return status == Status.LOSS_DIPLOMATIC; }
    public boolean lostDiplomaticAsLeader()        { return lostDiplomatic() && isLeader(); }
    public boolean lostDiplomaticAsChallenger()    { return lostDiplomatic() && isChallenger(); }
    public boolean lostDiplomaticAsNonCandidate()  { return lostDiplomatic() && isNonCandidate(); }
    public boolean lostNewRepublic()               { return status == Status.LOSS_NEW_REPUBLIC; }
    private boolean lostNewRepublicAsLeader()      { return lostNewRepublic() && isLeader(); }
    public boolean lostNewRepublicAsFearedLeader() { return lostNewRepublicAsLeader() && isFeared(); }
    public boolean lostNewRepublicAsMajorLeader()  { return lostNewRepublicAsLeader() && isMajor(); }
    public boolean lostNewRepublicAsMinorLeader()  { return lostNewRepublicAsLeader() && isMinor(); }
    public boolean lostNewRepublicAlliance()       { return lostNewRepublicAsLeader() && allianceWithLeader; }
    public boolean lostRebellion()                 { return status == Status.LOSS_REBELLION; }
    public boolean lostRebellionAsCrazyLeader()    { return lostRebellion() && isLeader(); } // was original leader
    public boolean lostRebellionAsChallenger()     { return lostRebellion() && isChallenger(); }
    public boolean lostRebellionAsFollower()       { return lostRebellion() && isNonCandidate(); }
    public boolean wonMilitary()                   { return status == Status.WIN_MILITARY; }
    public boolean wonMilitaryAlliance()           { return status == Status.WIN_MILITARY_ALLIANCE; }
    public boolean wonDiplomatic()                 { return status == Status.WIN_DIPLOMATIC; }
    public boolean wonDiplomaticMinor()            { return wonDiplomatic() && isMinor(); }
    public boolean wonDiplomaticMajor()            { return wonDiplomatic() && isMajor(); }
    public boolean wonDiplomaticFeared()           { return wonDiplomatic() && isFeared(); }
    public boolean wonCouncilAlliance()            { return status == Status.WIN_COUNCIL_ALLIANCE; }
    public boolean wonNewRepublic()                { return status == Status.WIN_NEW_REPUBLIC; }
    public boolean wonNewRepublicAsFearedLeader()  { return wonNewRepublic() && isFeared(); }
    public boolean wonNewRepublicAsMajorLeader()   { return wonNewRepublic() && isMajor(); }
    public boolean wonNewRepublicAsMinorLeader()   { return wonNewRepublic() && isMinor(); }
    public boolean wonNewRepublicAsAllied()        { return wonNewRepublic() && allianceWithLeader; } // Not reachable yet
    public boolean wonNewRepublicAsChampion()      { return subStatus == wonNewRepublicAsChampion; } // Not reachable yet
    public boolean wonRebellion()                  { return status == Status.WIN_REBELLION; }
    public boolean wonRebellionAsCrazyLeader()     { return wonRebellion() && isLeader(); } // was original leader
    public boolean wonRebellionAsChallenger()      { return wonRebellion() && isChallenger(); }
    public boolean wonRebellionAsFollower()        { return wonRebellion() && isNonCandidate(); }
    public boolean wonRebellionAlliance()           { return status == Status.WIN_REBELLION_ALLIANCE; }
    public boolean wonRebellionAllianceAsLeader()   { return wonRebellionAlliance() && isChallenger(); }
    public boolean wonRebellionAllianceAsFollower() { return wonRebellionAlliance() && isNonCandidate(); }
    
    public void startGame()               { status = Status.IN_PROGRESS; }
    // Never happen!
    public void loseOverthrown()          { status = Status.LOSS_OVERTHROWN; }
    // All colony lost...
    public void loseMilitary()            { status = Status.LOSS_MILITARY; }
    // All colony lost as ally
    public void loseNewRepublicAsAllied() { status = Status.LOSS_MILITARY; }
    // Accept council decision
    public void loseDiplomatic()          { status = Status.LOSS_DIPLOMATIC; }
    // player was leader of alliance and still lost!
    public void loseNewRepublic()         { status = Status.LOSS_NEW_REPUBLIC; }
    // player was a rebel leader against alliance and lost
    public void loseRebellion()           { status = Status.LOSS_REBELLION; }
    // player was following rebels against alliance and lost
    public void loseRebellionAsFollower() { status = Status.LOSS_REBELLION; }
    // no one killed us... abandonment suicide
    public void loseNoColonies()          { status = Status.LOSS_NO_COLONIES; }
    // if player won the vote and no rebels, game over;
    public void winDiplomatic()           { status = Status.WIN_DIPLOMATIC; }
    // if only one empire is left then player must have won
    public void winMilitary()             { status = Status.WIN_MILITARY; }
    // multiple empires, all allied with player.. that's a win
    public void winMilitaryAlliance()     { status = Status.WIN_MILITARY_ALLIANCE; }
    // rebellion has been defeated; Player = Leader
    public void winNewRepublic()          { status = Status.WIN_NEW_REPUBLIC; }
    // rebellion has been defeated; Player = Allies
    public void winNewRepublicAsAllied()  {
    	status = Status.WIN_NEW_REPUBLIC;
    	subStatus = wonNewRepublicAsAllied;
    }
    // rebellion has been defeated; Player = Allies; leader was defeated
    public void winNewRepublicAsChampion()  {
    	status = Status.WIN_NEW_REPUBLIC;
    	subStatus = wonNewRepublicAsChampion;
    }
    // New Republic has been defeated;  Player = Leader
    public void winRebellion()            { status = Status.WIN_REBELLION; }
    // rebellion win;
    public void winRebellionAlliance()    { status = Status.WIN_REBELLION_ALLIANCE; }
    public void winRebellionAsFollower()  { status = Status.WIN_REBELLION; }
    public void winRebellionAllianceAsFollower() { status = Status.WIN_REBELLION_ALLIANCE; }
    public void winCouncilAlliance()      { status = Status.WIN_COUNCIL_ALLIANCE; }

    
    
    
}
