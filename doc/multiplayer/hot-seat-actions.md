# Hot-seat action table

Local UI actions require the active planning owner and current viewer generation. Covered handoffs, resolution, finished seats and callbacks from older views reject planning edits. These checks are a local desktop boundary, not a network command/security protocol.

| Action family | Support and validation |
| --- | --- |
| Fleet deployment/undeployment | Active owner's fleets; existing range, destination and ship-count rules apply. |
| Rally points and forwarding | Active owner's source colonies; normal rally eligibility applies. |
| Transports | Active owner's source colonies; amount bounded by available population; normal destination eligibility applies. Mixed-owner bulk selections are rejected before mutation. |
| Colony spending, locks, bases, governor and ship requests | Active owner's colony or wholly owned bulk selection. Existing spending controls bound amounts. |
| Reserve transfers and colony budgets | Active owner's treasury and wholly owned target selection. |
| Ship designs, rename, scrap and colors | Current owner's design UI; confirmation callbacks also validate the design owner. |
| Research allocation and locks | Current planning owner's research tree; stale panels reject callbacks. |
| Spies and missions | Current owner's empire views and valid row selection. |
| Human trade, peace, pact, alliance and joint-war offers | Valid current-owner revision, living/contacted recipient, eligible treaty state/amount/target, duplicate suppression. Recipient explicitly answers at resolution. Both parties get private replies. |
| AI diplomacy | Existing AI diplomacy behavior through the active owner's screen. |
| Human technology barter/counteroffers/threats | Disabled in hot-seat menus. |
| Council, research, colonization, diplomacy choices | Covered owner handoff, legal options, one accepted answer per pending decision. Saveable boundaries. |
| Bombardment, espionage, sabotage | Covered owner handoff and legal options. Complete the synchronous choice before saving. |
| Space and ground combat | Automatic model resolution, recipient-specific reports. No manual space combat UI. |
| Shared game/governor policy editors | Frozen for the match; per-colony controls remain supported. |
| Autoplay, debug player swap, VIP console, turn replay | Unavailable during hot-seat play. |

Automated coverage includes real UI callbacks for both owners, stale research callbacks, foreign designs/spy budgets/transports, mixed-owner governor changes, treaty acceptance/refusal, eight graphical prompt families, multiple rounds, eliminated seats, Council/military endings and checkpoint recovery. Graphical evidence and remaining acceptance items are listed in [the ledger](hot-seat-acceptance.md).
