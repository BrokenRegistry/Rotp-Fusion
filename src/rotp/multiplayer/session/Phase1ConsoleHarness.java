package rotp.multiplayer.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rotp.model.game.GameSession;
import rotp.model.game.IGalaxyOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.RulesetManager;
import rotp.multiplayer.turn.InProcessBombardmentDecisionAdapter;
import rotp.multiplayer.turn.InProcessEspionageDecisionAdapter;
import rotp.multiplayer.turn.InProcessMatchDriver;
import rotp.multiplayer.turn.InProcessSabotageDecisionAdapter;
import rotp.multiplayer.turn.PendingColonizationDecision;
import rotp.multiplayer.turn.PendingDecision;
import rotp.multiplayer.turn.PendingDiplomacyDecision;
import rotp.multiplayer.turn.PendingResearchDecision;
import rotp.multiplayer.turn.StrictInProcessNotificationSink;
import rotp.multiplayer.turn.TurnCheckpoint;

/** Interactive, local-only Phase 1 exercise; not a multiplayer client. */
public final class Phase1ConsoleHarness {
    private static final BufferedReader INPUT = new BufferedReader(new InputStreamReader(System.in));

    private Phase1ConsoleHarness() { }

    public static void run() {
        try {
            IGameOptions options = RulesetManager.current().newOptions().copyAllOptions();
            options.selectedGalaxySize(IGalaxyOptions.SIZE_TINY);
            options.selectedNumberOpponents(2);
            ControllerRegistry registry = new ControllerRegistry();
            registry.add(new PlayerSeat("human-1", 0, PlayerSeat.ControllerType.HUMAN,
                    PlayerSeat.ConnectionStatus.CONNECTED));
            registry.add(new PlayerSeat("human-2", 1, PlayerSeat.ControllerType.HUMAN,
                    PlayerSeat.ConnectionStatus.CONNECTED));
            GameSession session = GameSession.instance();
            session.startGame(options, registry);
            installProviders(session);
            InProcessMatchDriver driver = new InProcessMatchDriver(session, Map.of(
                    "human-1", new ConsoleDecisions("human-1"),
                    "human-2", new ConsoleDecisions("human-2")));

            System.out.println("Phase 1 in-process game started: human-1=empire 0, "
                    + "human-2=empire 1, empire 2=AI.");
            System.out.println("Console commands: turn, status, quit. "
                    + "Use this console for turns; the desktop is not a multiplayer client.");
            while (true) {
                String command = ask("phase1> ").trim().toLowerCase();
                if (command.equals("quit")) {
                    System.exit(0);
                    return;
                }
                if (command.equals("status")) {
                    System.out.println("Turn " + session.galaxy().currentTurn()
                            + ", outcome " + session.matchOutcome());
                    continue;
                }
                if (!command.equals("turn")) {
                    System.out.println("Enter turn, status, or quit.");
                    continue;
                }
                if (!session.inProgress()) {
                    System.out.println("Match finished: " + session.matchOutcome());
                    continue;
                }
                TurnCheckpoint checkpoint = driver.advanceOneTurn();
                System.out.println("Completed turn " + checkpoint.turn() + " at "
                        + checkpoint.lastCompletedPhase() + "; " + session.matchOutcome());
                for (PlayerSeat seat : registry.seats()) {
                    if (seat.controllerType() == PlayerSeat.ControllerType.HUMAN)
                        System.out.println(seat.playerId() + " alerts: "
                                + session.alertRecordsForEmpire(seat.empireId()).size());
                }
            }
        }
        catch (Exception failure) {
            failure.printStackTrace(System.err);
            System.err.println("Phase 1 harness stopped at the failure above.");
        }
    }

    private static void installProviders(GameSession session) {
        Map<String, StrictInProcessNotificationSink.ScoutingNoticeProvider> scouting = new HashMap<>();
        Map<String, StrictInProcessNotificationSink.TechnologyNoticeProvider> technology = new HashMap<>();
        Map<String, StrictInProcessNotificationSink.ShipConstructionNoticeProvider> construction = new HashMap<>();
        Map<String, StrictInProcessNotificationSink.DiplomacyNoticeProvider> diplomacy = new HashMap<>();
        Map<String, StrictInProcessNotificationSink.BombardmentNoticeProvider> bombardment = new HashMap<>();
        Map<String, StrictInProcessNotificationSink.SabotageNoticeProvider> sabotage = new HashMap<>();
        Map<String, InProcessEspionageDecisionAdapter.Provider> espionageChoices = new HashMap<>();
        Map<String, InProcessBombardmentDecisionAdapter.Provider> bombardmentChoices = new HashMap<>();
        Map<String, InProcessSabotageDecisionAdapter.Provider> sabotageChoices = new HashMap<>();
        for (PlayerSeat seat : session.controllerRegistry().seats()) {
            if (seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
                continue;
            String playerId = seat.playerId();
            scouting.put(playerId, (empireId, systems) ->
                    System.out.println(playerId + " scouted " + systems));
            technology.put(playerId, notice ->
                    System.out.println(playerId + " technology " + notice));
            construction.put(playerId, (empireId, designs) ->
                    System.out.println(playerId + " ships " + designs));
            diplomacy.put(playerId, notice -> {
                System.out.println(playerId + " diplomacy information " + notice);
                return false;
            });
            bombardment.put(playerId, notice ->
                    System.out.println(playerId + " bombardment result " + notice));
            sabotage.put(playerId, notice ->
                    System.out.println(playerId + " sabotage result " + notice));
            espionageChoices.put(playerId, decision -> {
                System.out.println(playerId + " espionage " + decision);
                String techId = decision.categoryTechIds().values().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("No espionage technology"));
                return new InProcessEspionageDecisionAdapter.Choice(techId, null);
            });
            bombardmentChoices.put(playerId, decision -> {
                System.out.println(playerId + " bombardment " + decision);
                return yesNo(playerId + " bombard? ")
                        ? InProcessBombardmentDecisionAdapter.Choice.BOMBARD
                        : InProcessBombardmentDecisionAdapter.Choice.SKIP;
            });
            sabotageChoices.put(playerId, decision -> {
                System.out.println(playerId + " sabotage " + decision);
                System.out.println("Sabotage is cancelled in this console exercise.");
                return null;
            });
        }
        session.turnNotificationSink(StrictInProcessNotificationSink.deferredDecisions(
                scouting, technology, construction, diplomacy, bombardment, sabotage));
        session.espionageDecisionAdapter(new InProcessEspionageDecisionAdapter(espionageChoices));
        session.bombardmentDecisionAdapter(new InProcessBombardmentDecisionAdapter(bombardmentChoices));
        session.sabotageDecisionAdapter(new InProcessSabotageDecisionAdapter(sabotageChoices));
    }

    private static final class ConsoleDecisions implements InProcessMatchDriver.SeatDecisions {
        private final String playerId;
        private ConsoleDecisions(String playerId) { this.playerId = playerId; }

        @Override public Integer councilVote(PendingDecision decision) {
            System.out.println(playerId + " council vote " + decision.id()
                    + " choices " + decision.legalEmpireIds());
            String reply = ask("Empire ID, or blank to abstain: ").trim();
            return reply.isEmpty() ? null : Integer.valueOf(reply);
        }
        @Override public boolean councilRuling(PendingDecision decision) {
            System.out.println(playerId + " council ruling " + decision.id());
            return yesNo("Accept the ruling? ");
        }
        @Override public String research(PendingResearchDecision decision) {
            List<String> choices = decision.legalTechIds();
            System.out.println(playerId + " research " + decision.id() + " choices " + choices);
            String reply = ask("Technology ID (blank for first): ").trim();
            return reply.isEmpty() ? choices.get(0) : reply;
        }
        @Override public boolean colonize(PendingColonizationDecision decision) {
            System.out.println(playerId + " colonization " + decision.id()
                    + " system " + decision.systemId());
            return yesNo("Colonize? ");
        }
        @Override public boolean diplomacy(PendingDiplomacyDecision decision) {
            System.out.println(playerId + " diplomatic offer " + decision.id()
                    + " " + decision.notice());
            return yesNo("Accept? ");
        }
    }

    private static boolean yesNo(String prompt) {
        return ask(prompt + "[y/N] ").trim().equalsIgnoreCase("y");
    }

    private static String ask(String prompt) {
        System.out.print(prompt);
        try {
            String line = INPUT.readLine();
            if (line == null)
                throw new IllegalStateException("Console input closed");
            return line;
        }
        catch (IOException failure) {
            throw new IllegalStateException("Console input failed", failure);
        }
    }
}
