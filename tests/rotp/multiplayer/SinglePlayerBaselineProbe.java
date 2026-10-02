package rotp.multiplayer;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import rotp.Rotp;
import rotp.model.empires.Empire;
import rotp.model.combat.ShipCombatManager;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.model.game.IMainOptions;
import rotp.model.game.IDebugOptions;
import rotp.model.game.RulesetManager;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.tech.TechCategory;
import rotp.model.tech.TechLibrary;
import rotp.ui.RotPUI;
import rotp.util.Rand;

/** Separate-process probe using only APIs present in the original single-player revision. */
public final class SinglePlayerBaselineProbe {
    public static void main(String[] args) {
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor(task -> {
            Thread watchdog = new Thread(task, "baseline-probe-timeout");
            watchdog.setDaemon(true);
            return watchdog;
        }).schedule(() -> {
            System.err.println("Error: baseline probe exceeded 180 seconds");
            Runtime.getRuntime().halt(1);
        }, 180, java.util.concurrent.TimeUnit.SECONDS);
        try {
            run(Path.of(args[0]).toAbsolutePath());
            System.exit(0);
        }
        catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void run(Path directory) throws Exception {
        Rotp.rand(new Rand(136L));
        Files.createDirectories(directory);
        // Animation frame selection consumes the shared RNG and depends on rendering timing.
        Files.writeString(directory.resolve("Remnants.cfg"), "GRAPHICS: Low\n");
        setStatic("startupDir", directory + File.separator);
        setStatic("isIDE", false);
        JFrame frame = new JFrame();
        setStatic("frame", frame);
        Method init = Rotp.class.getDeclaredMethod("initUtils");
        init.setAccessible(true);
        init.invoke(null);
        if (Rotp.startupException != null)
            throw new IllegalStateException("Engine startup failed", Rotp.startupException);
        RulesetManager.current();
        RotPUI ui = new RotPUI();
        frame.add(ui);
        ui.initModel();
        IGameOptions.galaxyRandSource.set(136);
        setStatic("initialized", true);
        Field timer = RotPUI.class.getDeclaredField("timer");
        timer.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            ui.init();
            try { ((Timer) timer.get(ui)).stop(); }
            catch (IllegalAccessException failure) { throw new IllegalStateException(failure); }
        });
        IMainOptions.saveDirectory.set(directory.toString());
        Rotp.rand(new Rand(136L));
        IGameOptions options = RulesetManager.current().newOptions().copyAllOptions();
        options.selectedGalaxySize(IGameOptions.SIZE_TINY);
        options.selectedNumberOpponents(2);
        options.selectedCouncilWinOption(IGameOptions.COUNCIL_NONE);
        Rotp.rand(new Rand(136L));
        GameSession session = GameSession.instance();
        session.startGame(options);
        // Existing debug mode suppresses presentation in both revisions, without autoplay ownership.
        IDebugOptions.debugAutoRun.set("On");
        IGameOptions.showAllocatePopUp.set(false);
        List<String> output = new ArrayList<>();
        snapshot(session, output);
        Method turnProcess = GameSession.class.getDeclaredMethod("nextTurnProcess");
        turnProcess.setAccessible(true);
        for (int turn = 0; turn < 3; turn++) {
            desktopTurn(session, turnProcess);
            snapshot(session, output);
        }
        output.add("scenario=research-completion");
        Empire player = session.galaxy().player();
        TechCategory research = player.tech().computer();
        if (research.currentTech() == null)
            research.currentTech(TechLibrary.current().tech(research.techIdsAvailableForResearch().get(0)));
        String completed = research.currentTech();
        require(research.completeResearch(), "Research fixture needs an active project");
        desktopTurn(session, turnProcess);
        require(player.tech().allKnownTechs().contains(completed), "Research must actually complete");
        String nextResearch = research.techIdsAvailableForResearch().stream()
                .filter(id -> !player.tech().allKnownTechs().contains(id)).findFirst().orElseThrow();
        require(research.currentTech(TechLibrary.current().tech(nextResearch)),
                "The next research choice must be legal");
        require(!completed.equals(research.currentTech()), "Research must move to the next project");
        snapshot(session, output);

        output.add("scenario=colonization");
        StarSystem colonyTarget = Arrays.stream(session.galaxy().starSystems())
                .filter(s -> s != null && !s.isColonized() && !s.hasMonster()
                        && player.canColonize(s.planet().type())).findFirst().orElseThrow();
        int oldColonies = player.allColonizedSystems().size();
        session.galaxy().ships.buildShips(player.id, colonyTarget.id, player.shipLab().colonyDesign().id(), 1);
        colonyTarget.orbitingFleetForEmpire(player).colonizeSystem(colonyTarget, player.shipLab().colonyDesign());
        require(colonyTarget.empId() == player.id && player.allColonizedSystems().size() == oldColonies + 1,
                "Colonization must transfer ownership");
        snapshot(session, output);

        output.add("scenario=automatic-combat");
        Empire enemy = session.galaxy().empire(1);
        player.viewForEmpire(enemy).embassy().declareWar();
        StarSystem arena = Arrays.stream(session.galaxy().starSystems())
                .filter(s -> s != null && !s.isColonized() && !s.hasMonster()).findFirst().orElseThrow();
        session.galaxy().ships.buildShips(player.id, arena.id, player.shipLab().fighterDesign().id(), 40);
        session.galaxy().ships.buildShips(enemy.id, arena.id, enemy.shipLab().fighterDesign().id(), 3);
        ShipCombatManager.fleetAutoCombat.set("FLEET_AUTO_COMBAT_AUTO");
        ShipCombatManager.showAutoCombatResults.set(false);
        ShipCombatManager battle = session.galaxy().shipCombat();
        battle.battle(arena);
        require(battle.results() != null && battle.results().victor() != null,
                "Automatic battle needs a victor");
        require(arena.orbitingFleetsNoMonster().stream().map(ShipFleet::empId).distinct().count() <= 1,
                "Opposing fleets must resolve");
        output.add("combat-victor=" + battle.results().victor().id);
        snapshot(session, output);

        output.add("scenario=diplomacy");
        player.viewForEmpire(enemy).embassy().contact(true);
        enemy.viewForEmpire(player).embassy().contact(true);
        player.diplomatAI().acceptOfferPeace(enemy);
        require(!player.atWarWith(enemy.id), "Peace must end the war");
        player.diplomatAI().acceptOfferPact(enemy);
        require(player.pactWith(enemy.id), "Pact must be signed");
        player.diplomatAI().acceptOfferAlliance(enemy);
        require(player.alliedWith(enemy.id), "Alliance must be signed");
        snapshot(session, output);

        output.add("scenario=military-victory");
        // Use real extinction bookkeeping, without a manual game-status override.
        for (Empire empire : session.galaxy().empires())
            if (empire.id != player.id)
                for (StarSystem system : new ArrayList<>(empire.allColonizedSystems()))
                    system.colony().destroy();
        require(session.status().wonMilitary(), "Sole surviving player must win militarily");
        require(!session.status().inProgress(), "Finished game must stop");
        snapshot(session, output);
        for (int i = 0; i < 16; i++)
            output.add("rng=" + Rotp.rand().nextLong());

        // A separate seeded game preserves the independent military and Council endings.
        output.add("scenario=council-victory");
        options.selectedCouncilWinOption(IGameOptions.COUNCIL_IMMEDIATE);
        Rotp.rand(new Rand(136L));
        session.startGame(options);
        IDebugOptions.debugAutoRun.set("On");
        Empire councilPlayer = session.galaxy().player();
        councilPlayer.allColonizedSystems().get(0).colony().setPopulation(1000);
        var council = session.galaxy().council();
        Method openConvention = council.getClass().getDeclaredMethod("openConvention");
        openConvention.setAccessible(true);
        openConvention.invoke(council);
        while (council.votingInProgress()) {
            if (council.nextVoter().isPlayerControlled()) council.castPlayerVote(councilPlayer);
            else council.castNextVote();
        }
        require(council.leader() == councilPlayer, "The scripted Council must elect the player");
        council.acceptRuling(councilPlayer);
        require(session.status().wonDiplomatic(), "Council must award diplomatic victory");
        // Debug autorun deliberately continues with surviving opponents after victory.
        IDebugOptions.debugAutoRun.set("Off");
        require(!session.status().inProgress(), "Council victory must stop ordinary play");
        output.add("council=" + council.leader().id + ":" + council.votes1() + ":" + council.votes2());
        snapshot(session, output);
        for (int i = 0; i < 16; i++) output.add("council-rng=" + Rotp.rand().nextLong());
        Files.write(directory.resolve("state.txt"), output);
    }

    private static void desktopTurn(GameSession session, Method turnProcess) throws Exception {
        int before = session.galaxy().currentTurn();
        ((Runnable) turnProcess.invoke(session)).run();
        require(session.galaxy().currentTurn() == before + 1, "Desktop turn must advance once");
        try {
            Field failed = GameSession.class.getDeclaredField("turnFailed");
            failed.setAccessible(true);
            require(!failed.getBoolean(session), "Desktop turn failed");
        }
        catch (NoSuchFieldException originalRevision) {
            // The original revision predates this diagnostic. The runner also checks its log.
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void snapshot(GameSession session, List<String> output) {
        List<String> state = new ArrayList<>();
        state.add("turn=" + session.galaxy().currentTurn());
        state.add("status=" + session.status().inProgress() + ":" + session.status().won()
                + ":" + session.status().lost() + ":" + session.status().wonMilitary());
        for (Empire empire : session.galaxy().empires()) {
            state.add("empire=" + empire.id + ":" + empire.totalReserve() + ":"
                    + empire.tech().allKnownTechs().stream().sorted().toList());
            for (int i = 0; i < 6; i++) {
                TechCategory category = empire.tech().category(i);
                state.add("research=" + empire.id + ":" + i + ":" + category.currentTech()
                        + ":" + category.totalBC() + ":" + category.allocation());
            }
            for (ShipFleet fleet : empire.allFleets())
                state.add("fleet=" + empire.id + ":" + fleet.sysId() + ":" + fleet.destSysId()
                        + ":" + fleet.x() + ":" + fleet.y() + ":" + Arrays.toString(fleet.numCopy()));
            for (Empire other : session.galaxy().empires())
                if (empire != other) state.add("diplomacy=" + empire.id + ":" + other.id + ":"
                        + empire.viewForEmpire(other).embassy().treaty().getClass().getSimpleName()
                        + ":" + empire.viewForEmpire(other).trade().level());
        }
        for (StarSystem system : session.galaxy().starSystems()) {
            if (system == null || !system.isColonized())
                continue;
            var colony = system.colony();
            state.add("colony=" + system.id + ":" + system.empId() + ":" + colony.population()
                    + ":" + colony.industry().factories() + ":" + colony.defense().rawBases());
            for (int i = 0; i < 5; i++)
                state.add("spending=" + system.id + ":" + i + ":" + colony.allocation(i));
        }
        state.sort(String::compareTo);
        output.addAll(state);
    }

    private static void setStatic(String name, Object value) throws Exception {
        Field field = Rotp.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

}
