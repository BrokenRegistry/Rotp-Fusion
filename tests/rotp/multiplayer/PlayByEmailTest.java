package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.empires.Empire;
import rotp.model.empires.EspionageMission;
import rotp.model.empires.SabotageMission;
import rotp.model.empires.Spy;
import rotp.model.game.GameSession;
import rotp.model.game.IConvenienceOptions;
import rotp.model.game.IGameOptions;
import rotp.model.galaxy.StarSystem;
import rotp.model.tech.TechLibrary;
import rotp.multiplayer.hotseat.*;
import rotp.multiplayer.pbem.StandingOrders;
import rotp.multiplayer.pbem.StandingOrders.*;
import rotp.multiplayer.turn.*;
import rotp.ui.multiplayer.HotSeatDesktop;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class PlayByEmailTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    /** Fails the test if a mid-phase choice ever reaches a screen in play by email. */
    static class NoMissionPrompts extends HotSeatTurnTest.ImmediateChoices {
        @Override public CompletableFuture<Integer> councilVote(PendingDecision d) { return CompletableFuture.completedFuture(0); }
        @Override public CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> bombardment(BombardmentDecision d) { throw new AssertionError("bombard prompt"); }
        @Override public CompletableFuture<InProcessEspionageDecisionAdapter.Choice> espionage(EspionageDecision d) { throw new AssertionError("espionage prompt"); }
        @Override public CompletableFuture<InProcessSabotageDecisionAdapter.Choice> sabotage(SabotageDecision d) { throw new AssertionError("sabotage prompt"); }
    }

    static GameSession startMatch() {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 1))), true);
        return game;
    }

    static HotSeatController attach(GameSession game) {
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new NoMissionPrompts());
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller[0]); }
            catch (Exception ex) { throw new AssertionError(ex); }
            controller[0].start();
        });
        return controller[0];
    }

    static final java.util.Map<String, String> PINS = java.util.Map.of("a", "1111", "b", "2222");

    /** Clicks the button with this label, as a player would. */
    static void click(java.awt.Container root, String label) {
        var stack = new java.util.ArrayDeque<java.awt.Component>(List.of(root));
        while (!stack.isEmpty()) {
            var c = stack.pop();
            if (c instanceof javax.swing.AbstractButton b && label.equals(b.getText())) { b.doClick(0); return; }
            if (c instanceof java.awt.Container k) for (var child : k.getComponents()) stack.push(child);
        }
        fail("No button labelled " + label);
    }

    static boolean planningReady(GameSession game) {
        if (game.hotSeatState().snapshot().stage() != HotSeatState.Stage.PLANNING) return false;
        // Reports play on the game's own screens over the map: click through each one.
        if (GameSession.performingTurn()) { game.resumeNextTurnProcessing(); return false; }
        return !HotSeatDesktop.blocksNavigation();
    }

    static void awaitPlanning(GameSession game) throws Exception {
        play(g -> planningReady(g), new java.util.ArrayList<>(), java.util.Set.of());
    }

    /**
     * Plays as every person in turn: types PINs, sends turn files and opens them again as the
     * recipient would, until done. Players named in wrongPinFirst first try a wrong PIN.
     */
    static GameSession play(java.util.function.Predicate<GameSession> done, List<String> sent,
            java.util.Set<String> wrongPinFirst) throws Exception {
        var tried = new java.util.HashSet<String>();
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(300);
        while (true) {
            GameSession game = GameSession.instance();
            if (done.test(game)) return game;
            assertTrue(System.nanoTime() < deadline, "Timed out: " + game.hotSeatState().snapshot());
            assertNotEquals(HotSeatState.Stage.ERROR, game.hotSeatState().snapshot().stage());
            java.io.File[] next = new java.io.File[1];
            if (planningReady(game)) HotSeatTestFixture.onEdt(() -> {
                var snapshot = game.hotSeatState().snapshot();
                assertTrue(game.hotSeatController().finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision()));
                var finish = (rotp.ui.multiplayer.PlayByEmailFinishPanel) rotp.Rotp.getFrame().getGlassPane();
                finish.send(finish.orders());
            });
            HotSeatTestFixture.onEdt(() -> {
                var glass = rotp.Rotp.getFrame().getGlassPane();
                if (!glass.isVisible()) return;
                String owner = game.hotSeatState().snapshot().ownerPlayerId();
                if (glass instanceof rotp.ui.multiplayer.HotSeatPrivacyPane pin && pin.asksForPin()) {
                    if (wrongPinFirst.contains(owner) && tried.add(owner)) {
                        pin.enterPin("0000");
                        assertEquals(GameSession.instance().text("PBEM_WRONG_PIN"), pin.errorText());
                        assertSame(pin, rotp.Rotp.getFrame().getGlassPane(), "A wrong PIN must not unlock");
                    } else pin.enterPin(PINS.get(owner));
                } else if (glass instanceof rotp.ui.multiplayer.PlayByEmailSendPanel send && !send.isFinalResult()) {
                    next[0] = game.playByEmailSentFile();
                    send.primary();
                } else if (glass instanceof rotp.ui.multiplayer.HotSeatReportsPanel reports) reports.acknowledge();
            });
            if (next[0] != null) {
                sent.add(next[0].getName());
                open(next[0]);
            }
            Thread.sleep(20);
        }
    }

    /** Opens a turn file as its recipient, with scripted answers for the recipient's choices. */
    static GameSession open(java.io.File file) throws Exception {
        var restored = HotSeatPersistence.load(file);
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
        boolean[] covered = new boolean[1];
        while (!covered[0]) {
            assertTrue(System.nanoTime() < deadline, "A loaded turn must open behind a PIN screen");
            HotSeatTestFixture.onEdt(() -> covered[0] = rotp.Rotp.getFrame().getGlassPane()
                    instanceof rotp.ui.multiplayer.HotSeatPrivacyPane pin && pin.isVisible() && pin.asksForPin());
            Thread.sleep(20);
        }
        HotSeatTestFixture.onEdt(() -> {
            restored.hotSeatController().close();
            var controller = new HotSeatController(restored, new HotSeatDesktop(), new NoMissionPrompts());
            try { HotSeatTestFixture.set(GameSession.class, restored, "hotSeatController", controller); }
            catch (Exception ex) { throw new AssertionError(ex); }
            controller.resume();
        });
        return restored;
    }

    @Test void aRoundTripByFilesNeedsEachPlayersPinAndKeepsStandingOrders() throws Exception {
        var game = startMatch();
        int firstTurn = game.galaxy().currentTurn();
        var controller = attach(game);
        try {
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
            rotp.ui.multiplayer.HotSeatPrivacyPane[] pin = new rotp.ui.multiplayer.HotSeatPrivacyPane[1];
            while (pin[0] == null) {
                assertTrue(System.nanoTime() < deadline, "Alice must be asked to choose a PIN");
                HotSeatTestFixture.onEdt(() -> {
                    if (rotp.Rotp.getFrame().getGlassPane() instanceof rotp.ui.multiplayer.HotSeatPrivacyPane p
                            && p.isVisible() && p.asksForPin()) pin[0] = p;
                });
                Thread.sleep(20);
            }
            HotSeatTestFixture.onEdt(() -> {
                pin[0].enterPin("12");
                assertEquals(game.text("PBEM_PIN_INVALID"), pin[0].errorText());
                assertFalse(game.playByEmail().hasPin("a"));
            });
            List<String> sent = new java.util.ArrayList<>();
            play(g -> planningReady(g), sent, java.util.Set.of());
            assertTrue(game.playByEmail().hasPin("a"));
            assertEquals(List.of(), sent, "The first player plays on the computer that created the match");

            var orders = new StandingOrders(Bombard.ALWAYS, Frame.WHEN_POSSIBLE, SabotageTarget.MISSILES);
            HotSeatTestFixture.onEdt(() -> {
                var snapshot = game.hotSeatState().snapshot();
                assertTrue(controller.finishPlayerTurn("a", snapshot.revision()));
                var finish = (rotp.ui.multiplayer.PlayByEmailFinishPanel) rotp.Rotp.getFrame().getGlassPane();
                assertEquals(StandingOrders.DEFAULT, finish.orders());
                for (String key : List.of("PBEM_BOMBARD_ALWAYS", "PBEM_FRAME_WHEN_POSSIBLE", "PBEM_SABOTAGE_MISSILES"))
                    click(finish, game.text(key));
                assertEquals(orders, finish.orders(), "Clicking an option selects it");
                click(finish, game.text("PBEM_SEND"));
            });
            assertEquals(orders, game.playByEmail().orders("a"));

            var done = play(g -> g != game && planningReady(g) && g.galaxy().currentTurn() == firstTurn + 3,
                    sent, java.util.Set.of("a"));
            System.out.println("Turn files sent: " + sent);
            assertTrue(sent.size() >= 6, "Three rounds need at least two files each: " + sent);
            assertTrue(sent.get(0).matches("PBEM-\\d{8}-\\d{4}-T\\d{3}-r\\d{4}-for-Bob[.]rotp"), sent.toString());
            assertTrue(sent.get(sent.size() - 1).endsWith("-for-Alice.rotp"), sent.toString());
            assertEquals("a", done.hotSeatState().snapshot().ownerPlayerId());
            assertEquals(orders, done.playByEmail().orders("a"), "Standing orders travel with the file");
            assertTrue(done.playByEmail().hasPin("b"));
            assertFalse(done.playByEmail().pinMatches("b", "1111"));
            HotSeatTestFixture.onEdt(done.hotSeatController()::close);
        } finally { HotSeatTestFixture.onEdt(controller::close); }
    }

    @Test void theFinalResultIsSavedForEveryPlayerAndOpensWithoutAPin() throws Exception {
        var game = HotSeatTestFixture.start(2, 0);
        var options = game.options().copyAllOptions();
        options.selectedCouncilWinOption(IGameOptions.COUNCIL_IMMEDIATE);
        game.startHotSeatGame(options, new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 1))), true);
        game.galaxy().empire(0).allColonizedSystems().get(0).colony().setPopulation(1000);
        HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "nextAction", 2);
        HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "actionCountdown", 0);
        attach(game);
        List<String> sent = new java.util.ArrayList<>();
        var ended = play(g -> rotp.Rotp.getFrame().getGlassPane() instanceof rotp.ui.multiplayer.PlayByEmailSendPanel p
                && p.isVisible() && p.isFinalResult(), sent, java.util.Set.of());
        java.io.File finalFile = ended.playByEmailSentFile();
        assertTrue(finalFile.getName().matches("PBEM-\\d{8}-\\d{4}-T\\d{3}-final[.]rotp"), finalFile.getName());
        assertTrue(finalFile.isFile());
        assertFalse(sent.isEmpty(), "A Council vote for the absent player travels by file: " + sent);
        assertEquals(new java.util.HashSet<>(sent).size(), sent.size(), "Every handoff has its own file: " + sent);
        HotSeatTestFixture.onEdt(() -> {
            ((rotp.ui.multiplayer.PlayByEmailSendPanel) rotp.Rotp.getFrame().getGlassPane()).primary();
            assertInstanceOf(rotp.ui.multiplayer.HotSeatResultPanel.class, rotp.Rotp.getFrame().getGlassPane());
        });
        HotSeatTestFixture.onEdt(ended.hotSeatController()::close);

        var opened = HotSeatPersistence.load(finalFile);
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
        boolean[] results = new boolean[1];
        while (!results[0]) {
            assertTrue(System.nanoTime() < deadline, "The final file must open on the result screen");
            HotSeatTestFixture.onEdt(() -> {
                var glass = rotp.Rotp.getFrame().getGlassPane();
                assertFalse(glass instanceof rotp.ui.multiplayer.HotSeatPrivacyPane p && p.isVisible() && p.asksForPin(),
                        "Results are public; no PIN");
                results[0] = glass instanceof rotp.ui.multiplayer.HotSeatResultPanel && glass.isVisible();
            });
            Thread.sleep(20);
        }
        assertEquals(rotp.multiplayer.session.MatchOutcome.Cause.COUNCIL, opened.matchOutcome().cause());
        HotSeatTestFixture.onEdt(opened.hotSeatController()::close);
    }

    @Test void standingOrdersBombardWithoutPromptingAndIgnoreTheSharedSetting() throws Exception {
        var game = startMatch();
        assertNotNull(game.playByEmail());
        var controller = attach(game);
        String shared = IConvenienceOptions.autoBombard_.get();
        try {
            awaitPlanning(game);
            IConvenienceOptions.autoBombard_.set(IConvenienceOptions.AUTOBOMBARD_NEVER);
            Empire attacker = game.galaxy().empire(0);
            Empire defender = game.galaxy().empire(1);
            StarSystem target = defender.allColonizedSystems().get(0);
            attacker.viewForEmpire(defender).embassy().declareWar();
            game.galaxy().ships.buildShips(0, target.id, attacker.shipLab().bomberDesign().id(), 10);
            attacker.sv.refreshFullScan(target.id);
            var fleet = target.orbitingFleetForEmpire(attacker);

            game.playByEmail().orders("a", new StandingOrders(Bombard.NEVER, Frame.NEVER, SabotageTarget.FACTORIES));
            float before = target.colony().population();
            attacker.ai().promptForBombardment(target, fleet);
            assertEquals(before, target.colony().population(), "NEVER must not bomb");

            game.playByEmail().orders("a", new StandingOrders(Bombard.AT_WAR, Frame.NEVER, SabotageTarget.FACTORIES));
            attacker.ai().promptForBombardment(target, fleet);
            assertTrue(!target.isColonized() || target.colony().population() < before,
                    "AT_WAR must bomb even though the shared setting says never");
        } finally {
            IConvenienceOptions.autoBombard_.set(shared);
            HotSeatTestFixture.onEdt(controller::close);
        }
    }

    @Test void standingOrdersStealFrameAndSabotageWithoutPrompting() throws Exception {
        var game = startMatch();
        var controller = attach(game);
        try {
            awaitPlanning(game);
            Empire owner = game.galaxy().empire(0);
            Empire victim = game.galaxy().empire(1);
            StarSystem target = victim.allColonizedSystems().get(0);
            owner.viewForEmpire(victim).embassy().contact(true);
            victim.viewForEmpire(owner).embassy().contact(true);
            owner.sv.refreshFullScan(target.id);
            var spies = owner.viewForEmpire(victim).spies();
            String techId = owner.tech().computer().allTechs().stream()
                    .filter(id -> !owner.tech().allKnownTechs().contains(id)).findFirst().orElseThrow();
            var tech = TechLibrary.current().tech(techId);
            var espionage = new EspionageMission(spies, new Spy(spies).makeSuper(), List.of(tech), target, List.of(tech));
            game.espionageDecisionAdapter().present(game, espionage, victim.id);
            assertTrue(owner.tech().allKnownTechs().contains(techId), "The game picks the only legal tech");

            game.playByEmail().orders("a", new StandingOrders(Bombard.NEVER, Frame.NEVER, SabotageTarget.FACTORIES));
            float factories = target.colony().industry().factories();
            game.sabotageDecisionAdapter().resolve(game, new SabotageMission(spies, new Spy(spies).makeSuper()), target.id);
            assertTrue(target.colony().industry().factories() < factories, "Standing sabotage destroys factories");
        } finally { HotSeatTestFixture.onEdt(controller::close); }
    }

    @Test void turnFilesCarryTheBuildAndAMismatchIsRefusedBeforeLoading() throws Exception {
        var game = startMatch();
        var controller = attach(game);
        try {
            awaitPlanning(game);
            var file = directory.resolve("stamp.rotp").toFile();
            HotSeatTestFixture.onEdt(() -> {
                try { HotSeatPersistence.save(game, file); }
                catch (Exception ex) { throw new AssertionError(ex); }
            });
            String stamp;
            try (var zip = new java.util.zip.ZipFile(file)) {
                assertEquals("GameSession.dat", zip.entries().nextElement().getName(), "Session stays first");
                var properties = new java.util.Properties();
                properties.load(zip.getInputStream(zip.getEntry(rotp.multiplayer.pbem.BuildStamp.ZIP_ENTRY)));
                stamp = properties.getProperty(rotp.multiplayer.pbem.BuildStamp.KEY);
            }
            assertEquals(rotp.multiplayer.pbem.BuildStamp.current(), stamp);

            var tampered = directory.resolve("other-build.rotp").toFile();
            try (var in = new java.util.zip.ZipFile(file);
                    var out = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(tampered))) {
                for (var entry : java.util.Collections.list(in.entries())) {
                    out.putNextEntry(new java.util.zip.ZipEntry(entry.getName()));
                    if (entry.getName().equals(rotp.multiplayer.pbem.BuildStamp.ZIP_ENTRY))
                        out.write((rotp.multiplayer.pbem.BuildStamp.KEY + "=someone-else\n").getBytes());
                    else in.getInputStream(entry).transferTo(out);
                    out.closeEntry();
                }
            }
            var refused = assertThrows(RuntimeException.class,
                    () -> GameSession.instance().loadSession("", tampered.getPath(), false));
            assertTrue(refused.getMessage().contains("someone-else"), refused.getMessage());
            assertSame(game, GameSession.instance(), "The current game is untouched");
        } finally { HotSeatTestFixture.onEdt(controller::close); }
    }

    @Test void simulationSettingsStoredPerComputerAreFrozenForTheMatch() throws Exception {
        String aggression = rotp.model.game.IInGameOptions.gameAgressiveness.get();
        startMatch();
        rotp.model.game.IInGameOptions.gameAgressiveness.set(IGameOptions.AGGRESSIV_ALWAYS_WAR);
        assertEquals(aggression, rotp.model.game.IInGameOptions.gameAgressiveness.get(),
                "A computer's own setting must not change a running match");
    }
}
