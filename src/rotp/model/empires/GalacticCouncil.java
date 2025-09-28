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
package rotp.model.empires;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameStatus;
import rotp.model.game.IGameOptions;
import rotp.model.game.IInGameOptions;
import rotp.model.incidents.CouncilVoteIncident;
import rotp.model.incidents.FinalWarIncident;
import rotp.model.tech.Tech;
import rotp.ui.NoticeMessage;
import rotp.ui.RotPUI;
import rotp.ui.diplomacy.DialogueManager;
import rotp.ui.notifications.CouncilVoteNotification;
import rotp.ui.notifications.GNNNotification;
import rotp.util.Base;

public class GalacticCouncil implements Base, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int CHECK = 0;
    private static final int SCHEDULE = 1;
    private static final int CONVENE = 2;

    private static final int INACTIVE = 0;
    private static final int ACTIVE = 1;
    private static final int DISBANDED = 2;
    private static final int FINAL_WAR = 3;

    private static final int noticeDuration = 5;
    private static final int interval = 20;

    private int nextAction = CHECK;
    private int currentStatus = INACTIVE;
    private int actionCountdown = 1;
    private Empire leader;
    private final List<Empire> rebels = new ArrayList<>();
    private final List<Empire> allies = new ArrayList<>();
    private Empire rebelLeader; // Challenger

    //convention variables - reset when convention starts
    private transient List<Empire> voters, empires;
    private transient int voteIndex = 0;
    private transient double[] votes; // BR: Double to avoid equality in forced end of game
    private transient double totalVotes, votes1, votes2, lastVotes, playerVotes;
    private transient double playerVotesRatio;
    private transient Empire candidate1, candidate2, lastVoter, lastVoted;

    public static float pctRequired()  { return IGameOptions.counciRequiredPct.get(); }  // BR:Made it adjustable
    public Empire rebelLeader()        { return rebelLeader; }
    public Empire leader()             { return leader; }
    public void leader(Empire e)       { leader = e; }
    public List<Empire>  allies()      { return allies; }
    public boolean finalWar()          { return !rebels.isEmpty(); }
    public void addAlly(Empire e)      { allies.add(e); }
    public void addRebel(Empire e)     { rebels.add(e); }
    public boolean isAllied(Empire e)  { return allies.contains(e); }

    public List<Empire> voters() {
        if (voters == null) 
            voters = new ArrayList<>(empires());
        return voters;
    }
    public List<Empire> empires() {
        if (empires == null) 
            initEmpires();
        return empires;
    }
    public void init() {
        if (galaxy().numActiveEmpires() > 2)
            nextAction = CHECK;
    }
    public void checkIfDisband() {
        int num = galaxy().numActiveEmpires();
        if (active() &&  (num < 3)) {
            if (num == 2)
                GNNNotification.notifyCouncil(text("GNN_END_COUNCIL"));
            end();
        }
    }

    public int nextCouncil() { // BR:
        switch (nextAction) {
        case CHECK:    return -1;
        case SCHEDULE: return actionCountdown + noticeDuration;
        case CONVENE:
        default:
        	return actionCountdown;
        }
    }
	public boolean isForcedEndOfGame()	{
		IGameOptions opts = options();
		return opts.turnLimitedEndOfGame() &&
				galaxy().currentTurn() > opts.selectedEndOfGameTurn();
	}
    public void nextTurn() {
        voters = null;
        empires = null;

		if (isForcedEndOfGame()) {
			if (galaxy().numActiveEmpires() >= 3 && options().turnLimitedCouncil()) {
				actionCountdown = 0;
				nextAction = CONVENE;
				convene();
				return;
			}
			else {
				openConvention(); // to initialize variables.
				leader = candidate1;
				rebelLeader = candidate2;
				votes1 = votes[0];
				votes2 = votes[1];
				end();
				return;
			}
		}

        if (options().noGalacticCouncil())
            return;
        if (galaxy().numActiveEmpires() < 3)
            return;
        if (disbanded())
            return;

        actionCountdown--;
        if (actionCountdown > 0)
            return;

        switch (nextAction) {
            case CHECK:    checkFormation(); break;
            case SCHEDULE: schedule(); break;
            case CONVENE:  convene(); break;
        }
    }
    public boolean inactive()         { return currentStatus == INACTIVE; }
    public boolean active()           { return currentStatus == ACTIVE; }
    public boolean disbanded()        { return currentStatus >= DISBANDED; }
    public boolean rebelion()         { return currentStatus >= FINAL_WAR; }
    private void checkFormation() {
        Galaxy gal = galaxy();
        int limit = (int) Math.ceil(gal.numStarSystems()*pctRequired());
        float colonized = 0;
        for (int i=0; i<gal.numStarSystems(); i++) {
            StarSystem sys = gal.system(i);
            if (sys.isColonized())
                colonized++;
        }
        if (colonized >= limit) {
            currentStatus = ACTIVE;
            schedule();
        }
    }
    private void schedule() {
        if (!options().isAutoPlay()) {
            galaxy().giveAdvice("MAIN_ADVISOR_COUNCIL");
            GNNNotification.notifyCouncil(text("GNN_FORM_COUNCIL"));
        }
        nextAction = CONVENE;
        actionCountdown = noticeDuration;
    }
    private void convene() {
        openConvention();
        CouncilVoteNotification.create();
    }
    public boolean votingInProgress()  { return voteIndex < voters().size(); }
    public boolean hasVoted(Empire e)  { return voters().indexOf(e) < voteIndex; }
	public long votes(Empire e)        { return (long) votes[voters().indexOf(e)]; }
    public Empire nextVoter()  { return voters().get(voteIndex); }
    public Empire candidate1() { return candidate1; }
    public Empire candidate2() { return candidate2; }
    public Empire lastVoter()  { return lastVoter; }
    public Empire lastVoted()  { return lastVoted; }
    public long totalVotes()    { return (long) totalVotes; }
//    public int votesToElect()  { return (int) Math.ceil(totalVotes * 2 / 3.0); }
	public int votesToElect()  {
		if (isForcedEndOfGame())
			return 0;
		return (int) Math.ceil(totalVotes * pctRequired());
	}
	public long votes1()		{ return (long) votes1; }
	public long votes2()		{ return (long) votes2; }
	public long lastVotes()		{ return (long) lastVotes; }
	public long nextVotes()		{ return (long) votes[voteIndex]; }
    public boolean hasLeader() { return leader != null; }
    public void castNextVote() {
        // will not cast vote for player
        if (!nextVoter().isPlayerControlled())
            castNextVote(nextVoter().diplomatAI().councilVoteFor(candidate1(), candidate2()));
    }
    public void castPlayerVote(Empire chosen) {
        if (nextVoter().isPlayer())
            castNextVote(chosen);
    }
    public void continueNonPlayerVoting() {
        while (votingInProgress() && !nextVoter().isPlayerControlled())
            castNextVote();
    }
    /*
    // Never used not updated for new options!
    public boolean nextVoteWouldElect(Empire emp) {
        if ((emp != candidate1())
        && (emp != candidate2()))
            return false;
        int votesAlreadyCast = emp == candidate1() ? votes1() : votes2();
        return (nextVotes() + votesAlreadyCast) >= votesToElect();
    } */
    private void castNextVote(Empire chosen) {
        lastVoter = empires.get(voteIndex);
        lastVotes = votes[voteIndex];

        if (chosen == candidate1) {
            lastVoter.lastCouncilVoteEmpId(id(candidate1));
            votes1 += lastVotes;
            lastVoted = candidate1;
        }
        else if (chosen == candidate2) {
            lastVoter.lastCouncilVoteEmpId(id(candidate2));
            votes2 += lastVotes;
            lastVoted = candidate2;
        }
        else {
            lastVoter.lastCouncilVoteEmpId(Empire.ABSTAIN_ID);
            lastVoted = null;
        }

        voteIndex++;
        if (!votingInProgress())
            closeConvention();
    }
    private void end() {
    	GameStatus status = session().status();
        currentStatus = DISBANDED;
        if (leader == null) // only two empire remaining
            return;
        // The Final war started
        currentStatus = FINAL_WAR;
        rotp.ui.notifications.TradeTechNotification.showSkipTechButton = true;
        boolean playerhasAlliance = player().alliedWith(leader.id);
        status.playerVotesRatio((float)playerVotesRatio);
        if (leader.isPlayer()) {
        	status.playerStatus(GameStatus.playerIsLeader);
        	status.allianceWithLeader(false);
        }
        else if (rebelLeader.isPlayer()) {
        	status.playerStatus(GameStatus.playerIsChallenger);
        	status.allianceWithLeader(playerhasAlliance);
        }
        else {
        	status.allianceWithLeader(playerhasAlliance);
        }

        boolean electedLeaderIsCrazy = rebels.contains(leader);
        if (electedLeaderIsCrazy) {
            Empire crazyEmpire = leader;
            if (crazyEmpire == candidate1) {
            	leader = candidate2;
            	rebelLeader = candidate1;
            }
            else {
            	leader = candidate1;
            	rebelLeader = candidate2;
            }
            allies.addAll(rebels);
            allies.remove(crazyEmpire);
            rebels.clear();
            rebels.add(crazyEmpire);
        }

        // if player won the vote and no rebels, game over
        if (leader.isPlayer()) {
            if (rebels.isEmpty()
            		|| options().immediateCouncilWin()
            		|| options().realmsBeyondCouncil()
            		|| isForcedEndOfGame()) {
                session().status().winDiplomatic();
                return;
            }
        }
        // if player accepted ruling, also game over
        else if (allies.contains(player()) || options().realmsBeyondCouncil()) {
            if (playerhasAlliance && !options().noAllianceCouncil())
                session().status().winCouncilAlliance();
            else
                session().status().loseDiplomatic();
            return;
        }

        // ==> final war:
        // Either player is rebelling against leader,
        // or player is leader and at least one AI is rebelling
        if (leader.isPlayerControlled())
            galaxy().giveAdvice("MAIN_ADVISOR_COUNCIL_RESISTED", leader.raceName());                   
        else 
            galaxy().giveAdvice("MAIN_ADVISOR_RESIST_COUNCIL");

        // all members of alliance declare final war on player or all rebels
        // everyone gets the incident first. Once Final War is declared, no
        // more incidents are checked
        for (Empire rebel: rebels) {
            for (Empire ally: allies) {
                FinalWarIncident.create(ally, leader, rebel);
            }
        }

        // all members of alliance declare final war on player or all rebels
        for (Empire rebel: rebels) {
            for (Empire ally: allies) {
                ally.viewForEmpire(rebel).embassy().declareFinalWar();
            }
        }
        // all members of alliance establish unity with each other
        // this ensures no spying costs and all learned techs traded freely
		int numAllies = allies.size();
		int emp1 = 1;
		long minDelay = 250;
		Long lastTime = System.currentTimeMillis()-minDelay;
		boolean shading = true;
		for (Empire ally1: allies) {
			int emp2 = 1;
			for (Empire ally2: allies) {
				if (ally1 != ally2) {
					if (System.currentTimeMillis() - lastTime >= minDelay) {
						String str = text("COUNCIL_ESTABLISH_UNITY", emp1, emp2, numAllies);
						NoticeMessage.resetSubstatus(str);
						RotPUI.instance().paintCouncilNotice(shading);
						shading = false;
						lastTime = System.currentTimeMillis();
					}
					EmpireView v = ally1.viewForEmpire(ally2);
					v.embassy().establishUnity();
				}
				emp2++;
			}
			emp1++;
		}
        // all members of alliance share techs with leader
        for (Empire ally: allies) {
            for (Tech tech : ally.tech().techsUnknownTo(leader, false))
                leader.tech().acquireTechThroughTrade(tech.id, ally.id);
        }
        // leader then shares all techs with allies
        for (Empire ally: allies) {
            for (Tech tech : leader.tech().techsUnknownTo(ally, false))
                ally.tech().acquireTechThroughTrade(tech.id, leader.id);
        }
        if (leader.isPlayerControlled()) {
            for (Empire rebel: rebels) 
                rebel.respond(DialogueManager.WARNING_REBELLING_AGAINST, leader);
        }
        else {
            for (Empire rebel: rebels) {
                if (!rebel.isPlayerControlled()) 
                    rebel.respond(DialogueManager.PRAISE_REBELLING_WITH, player(), leader, "leader");
            }
        }
    }
    private void openConvention() {
        initConventionVars();

        // calculate vote total for each empire
		if (isForcedEndOfGame()) {
			switch (options().endOfGameCondition()) {
				case IInGameOptions.END_OF_GAME_TURN_TEC_IND:
					for (int i = 0; i <empires.size(); i++) {
						Empire voter = empires.get(i);
						votes[i] = voter.empireNonDynaTechnoIndPower() / 100;
						totalVotes += votes[i];
						if (voter.isPlayer())
							playerVotes = votes[i];
					}
					break;
				case IInGameOptions.END_OF_GAME_TURN_POWER:
					for (int i = 0; i <empires.size(); i++) {
						Empire voter = empires.get(i);
						votes[i] = voter.empireNonDynaPower() / 100;
						totalVotes += votes[i];
						if (voter.isPlayer())
							playerVotes = votes[i];
					}
					break;
				default:
					for (int i = 0; i <empires.size(); i++) {
						Empire voter = empires.get(i);
						votes[i] = voter.totalPlanetaryPopulation() / 100;
						totalVotes += votes[i];
						if (voter.isPlayer())
							playerVotes = votes[i];
					}
					break;
			}
		}
		else 
			for (int i = 0; i <empires.size(); i++) {
				Empire voter = empires.get(i);
				votes[i] = (int) Math.ceil(voter.totalPlanetaryPopulation() / 100);
				totalVotes += votes[i];
				if (voter.isPlayer())
					playerVotes = votes[i];
			}
        playerVotesRatio = (float)playerVotes/totalVotes;

        log("Convening council. # empires: " + empires.size());
    }
    private void initEmpires() {
        empires = galaxy().activeEmpires();
		if (isForcedEndOfGame()) {
			switch (options().endOfGameCondition()) {
				case IInGameOptions.END_OF_GAME_TURN_TEC_IND:
					Collections.sort(empires, Empire.NON_DYNA_TEC_IND);
					break;
				case IInGameOptions.END_OF_GAME_TURN_POWER:
					Collections.sort(empires, Empire.NON_DYNA_POWER);
					break;
				default:
					Collections.sort(empires, Empire.TOTAL_POPULATION);
					break;
			}
		}
		else
			Collections.sort(empires, Empire.TOTAL_POPULATION);
    }
    private void initConventionVars() {
        initEmpires();
        votes = new double[empires().size()];
        votes1 = 0;
        votes2 = 0;
        candidate1 = empires.get(0);
        candidate2 = empires.get(1);
    	if (options().playerVotesFirst()) {
    		empires.remove(player());
    		empires.add(0, player());
    	}
    	else if (options().playerVotesLast()) {
    		empires.remove(player());
    		empires.add(player());
    	}
        voteIndex = 0;
        totalVotes = 0;
        lastVotes = 0;
        playerVotes = 0;
    }
    private void closeConvention() {
        // determine leader
        int minVotes = this.votesToElect();
        leader = null;
        rebelLeader = null;
		if (votes1 > votes2) { // both can be above the threshold!
			if (votes1 >= minVotes) {
				leader = candidate1;
				rebelLeader = candidate2;
			}
		}
		else if (votes2 >= minVotes) {
			leader = candidate2;
			rebelLeader = candidate1;
		}
        /* if (votes1 >= minVotes) {
        	leader = candidate1;
        	rebelLeader = candidate2;
        }
        else if (votes2 >= minVotes) {
        	leader = candidate2;
        	rebelLeader = candidate1;
        }*/

        List<Empire> allVoters = new ArrayList<>(empires);
        // if leader is elected, ask all empires to accept ruling
        if (leader != null) {
            rebels.addAll(allVoters);
            for (Empire c : allVoters)
            	c.diplomatAI().acceptCouncilRuling(this);
            return;
        }

        // create incidents between voters and the candidates
        for (Empire voter: empires) {
            Empire lastVote = galaxy().empire(voter.lastCouncilVoteEmpId());
            CouncilVoteIncident.create(candidate1.viewForEmpire(voter), lastVote, candidate2);
            CouncilVoteIncident.create(candidate2.viewForEmpire(voter), lastVote, candidate1);
        }

        // schedule next council
        nextAction = SCHEDULE;
        actionCountdown = interval;
    }
    public void defyRuling(Empire e) {
        empires.remove(e);
        if (empires.isEmpty())
            end();
    }
    public void acceptRuling(Empire e) {
        empires.remove(e);
        rebels.remove(e);
        allies.add(e);
        if (empires.isEmpty())
            end();
    }

    public void removeEmpire(Empire deadEmpire) {
    	boolean deadWasAllied  = allies.contains(deadEmpire);
        allies.remove(deadEmpire);
        rebels.remove(deadEmpire);
        
        if (deadEmpire.isPlayer()) {
            if (leader().isPlayer()) {
                // player was leader of alliance and still lost!
                session().status().loseNewRepublic();
                return;
            }
            else if (deadWasAllied) {
            	// Should not happen! as accepting as non leader ends the game
            	// But in case of an future option...
            	// player was part of alliance and still lost!
            	session().status().loseNewRepublicAsAllied();
                return;
            }
            else if (rebelLeader().isPlayer()) {
                // player was rebel leader against alliance and lost
                session().status().loseRebellion();
                return;
            }
            else {
	            // player was following rebels against alliance and lost
            	// player was not candidate
	            session().status().loseRebellionAsFollower();
                return;
            }
        }
        else { // Player is alive
        	if (rebels.isEmpty()) { // rebellion has been defeated
        		if (leader().isPlayer()) {
        			// leaded by player the rebellion has been defeated.
                    session().status().winNewRepublic();
                    return;
                }
        		else if (allies.contains(leader())) { // Not reachable yet
                	// player was part of winning alliance!
                	session().status().winNewRepublicAsAllied();
                    return;
                }
        		else { // Not reachable yet
                	// player was part of winning alliance! but leader has been defeated
                	session().status().winNewRepublicAsChampion();
                    return;                	
                }
        	}
        	if (allies.isEmpty()) { // New Republic has been defeated
        		if (rebelLeader().isPlayer()) {
        			// player was leading rebellion!
        			if (rebels.size() == 1) {
	                    session().status().winRebellion();
	                    return;
                    }
        			else {
	                    session().status().winRebellionAlliance();
	                    return;
                    }
        		}
        		else { // player was part of winning rebellion!
        			if (rebels.size() == 1) {
	                    session().status().winRebellionAsFollower();
	                    return;
                    }
        			else {
	                    session().status().winRebellionAllianceAsFollower();
	                    return;
                    }        				
        		}
        	}

//        	if (rebels.isEmpty() && leader().isPlayer())
//                // rebellion has been defeated
//                session().status().winNewRepublic();
//            else if (rebels.isEmpty() && deadWasAllied)
//                // rebellion has been defeated
//                session().status().winNewRepublicAsAllied();
//            else if (allies.isEmpty() && !leader().isPlayer()) {
//                // New Republic has been defeated
//                if (rebels.size() == 1) 
//                    session().status().winRebellion();
//                else
//                    session().status().winRebellionAlliance();
//            }               
        }
    }
//    private void ensureFullContact() {
//        List<Empire> emps = new ArrayList<>(galaxy().activeEmpires());
//        for (Empire emp1: galaxy().activeEmpires()) {
//            emps.remove(emp1);
//            for (Empire emp2: emps)
//                emp1.makeContact(emp2);
//        }
//    }
}