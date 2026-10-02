# Multiplayer action and decision inventory

This inventory is based on source inspection at `1dfe8bfb`. It identifies action families and representative entry points. Before Phase 2, trace every mouse, keyboard, governor, and VIP console route for each family and mark it covered in the command gateway. “Private” means the acting empire's details must not be sent to opponents unless existing visibility rules allow them.

| Action family | Current entry points and model mutations | Owner and phase | Data exposure / validation concern |
|---|---|---|---|
| Fleet deploy, split, undeploy, redirect | `ui/main/FleetPanel.java`, `model/galaxy/Ships.java` | Empire owner; planning order | Fleet composition, range, destination, movement lock; private until visible |
| Fleet rally and relocation | `ui/main/RallyPointPanel.java`, `ui/sprites/ShipRelocationSprite.java`, `model/galaxy/Ships.java` | Colony/fleet owner; planning order | Rally route and ship counts can reveal hidden forces |
| Population transport | `ui/main/TransportPanel.java`, `TransportDeploymentPanel.java`, `SystemTransportSprite.java`, `model/galaxy/StarSystem.java` | Origin colony owner; planning order | Capacity, destination, travel time, and ownership |
| Colony spending and queues | `ui/main/EmpireColonySpendingPane.java`, `ui/planets/MultiColonySpendingPane.java`, `model/colony/Colony.java` | Colony owner; planning order | Allocation sum, locks, production and projects are private |
| Reserve transfer and tax | `ui/planets/TransferReserveUI.java`, `model/empires/Empire.java`, `model/colony/Colony.java` | Empire owner; planning order | Funds must be validated atomically against all transfers |
| Governor toggles and policies | `ui/main/GovernorOptionsPanel.java`, `model/game/GovernorOptions.java`, `model/colony/Colony.java` | Empire/colony owner; planning order | Governor decisions must run under the correct owner |
| Research allocation | `ui/tech/AllocateTechUI.java`, `model/tech/TechTree.java`, `TechCategory.java` | Empire owner; planning order | Tech choices and research points are private |
| Select next research | `ui/notifications/SelectTechNotification.java`, `ui/tech/SelectNewTechUI.java` | Discovering empire; resolution decision | May interrupt a turn; legal choices depend on current category state |
| Ship design, create, scrap, rename | `ui/design/DesignUI.java`, `ConfirmCreateUI.java`, `ConfirmScrapUI.java`, `model/ships/ShipDesignLab.java` | Empire owner; planning order | Design limits, existing fleet/queue effects, and unknown enemy components |
| Spies, missions, sabotage | `ui/races/ManageSpiesUI.java`, `SabotageUI.java`, `model/empires/SpyNetwork.java` | Spy empire; plan or resolution decision | Target visibility, incident attribution, stolen tech choice |
| Diplomatic offers and replies | `ui/races/RacesDiplomacyUI.java`, `ui/diplomacy/`, `ui/notifications/DiplomaticNotification.java` | Initiator plans; recipient answers at resolution | Offer payload, contact, eligibility, and response timing |
| Council vote and response | `model/empires/GalacticCouncil.java`, `ui/GalacticCouncilUI.java` | Each eligible voter; resolution decision | Voter ownership, sequencing, public vote totals, final verdict |
| Colonize unowned system | `ui/notifications/ColonizeSystemNotification.java`, `ui/planets/ColonizePlanetUI.java` | Arriving fleet owner; resolution decision | Collision of two human colonizers and system visibility |
| Bombardment choice | `ui/notifications/BombardSystemNotification.java`, `ui/RotPUI.java`, `model/combat/ShipCombatManager.java` | Attacking fleet owner; resolution decision | Prompt and casualty report belong to relevant empires |
| Tactical combat actions | `ui/combat/ShipBattleUI.java`, `model/combat/ShipCombatManager.java` | Current combat participant; resolution decision | Manual action ownership, legal targets, retreat, private ship details |
| Automatic combat, invasions, production, espionage outcomes | `model/galaxy/Galaxy.java`, `StarSystem.java`, `model/empires/Empire.java`, `model/colony/Colony.java` | Server simulation; automatic rule | Fixed resolution order and recoverable random state |
| Research discovery, plunder, scouting, construction reports | `ui/notifications/`, `model/game/GameSession.java` | Result recipient; display notification, sometimes followed by decision | Notification routing must distinguish information from a blocking choice |
| Galaxy map, empire reports, ship status, tech tree | `ui/main/`, `ui/races/`, `ui/tech/`, `model/empires/EmpireView.java` | Viewer; display only | Build an empire-specific view; UI hiding alone cannot enforce secrecy |
| Save/load, ready, and host settings | `model/game/GameSession.java`, `ui/game/`, planned lobby | Local/host controls; session operation | Save boundary, locked match options, seat permissions |
| VIP console operations | `ui/vipconsole/VIPConsole.java` and view classes | Same owner/phase as corresponding graphical action | Console can bypass UI-only command checks |

## Resolution prompts found

`ui/RotPUI.java` pauses the turn for council, GNN, sabotage, ground battle, bombardment, ship battle, colonization, technology, espionage, diplomacy, allocation, scouting, spy report, and construction screens. Many screens merely display information and acknowledge it; others require a choice. `GameSession.waitUntilNextTurnCanProceed()` waits on a process-global flag. `ShipCombatManager` also invokes this pause/wait path. For Phase 1, each call must be classified as either a recipient-specific decision with a legal response or a notification that cannot block the server.

## Initial scenario catalogue

Capture semantic state before and after each scenario, with fixed starting state and persisted random stream. Compare empire/colony/fleet/tech/diplomatic outcomes and game status. Exclude UI position, timestamps, file paths, and generated image state.

1. AI empire takes an ordinary production and fleet movement turn.
2. Human research completes and chooses the next technology.
3. Colony ship arrives, colonizes, and changes system ownership.
4. Two empires fight an automatic ship battle, including retreat and losses.
5. A diplomatic offer is accepted or rejected and a council vote resolves.
6. A victory or loss condition ends the game.

The scenarios are **not yet executable headless**: the current turn method saves to disk, calls `RotPUI`, and blocks for local prompts. `tests/rotp/multiplayer/SimulationBaselineTest.java` currently characterizes only deterministic random state and its serialization. Phase 1 must add a simulation seam before this catalogue can become full automated turn fixtures.

October 1 update: [Phase 1 acceptance](phase1-acceptance.md) now provides real-engine fixtures and a separate original-revision single-player probe spanning these scenario families. The fixtures arrange selected inputs and compare selected gameplay state; they do not cover every branch (such as every retreat configuration). The harness uses hidden Swing initialization and remains display-dependent; fully headless execution belongs to Phase 5.
