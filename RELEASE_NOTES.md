Unofficial port of Create Crafts & Additions 1.7.0 to Fabric Minecraft 26.2, built against Create Fly. Verified on a real 26.2 dedicated server.

- The full electrical set is here: motors, alternators, connectors and wires, capacitors, tesla coils, rolling mills, modular accumulators, the portable energy interface and the servo motor
- Energy uses the standard Fabric energy API, so other Fabric tech mods can push and pull power directly
- Machines keep their stored energy when chunks reload, which they did not upstream
- Wire networks, machine inventories and fluid contents survive server restarts
- Recipes and data updated for 26.2 item, tag and fluid changes
- Seed oil is now made in a basin by mixing rather than compacting
- ComputerCraft peripherals work when CC:Tweaked is installed
- Electric motor, alternator and rolling mill show their spinning shafts
- In-game Ponder scenes are included
- JEI recipe-viewer support: rolling, charging and liquid burning categories, with the rolling mill shown in sequenced assembly

Requires Fabric API and Create Fly 6.0.9-1 on Java 25. Client visuals and JEI are new in this build — please report any rendering issues. The rolling mill's JEI icon no longer spins (Create Fly has no public API for animated recipe widgets yet).
