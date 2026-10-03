package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rotp.model.combat.ShipCombatManager;
import rotp.model.game.IInGameOptions;
import rotp.model.game.IMainOptions;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.IParam;

/** Match-wide governor defaults and per-computer rules are immutable during a match. */
public final class HotSeatPolicies implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, String> values = new LinkedHashMap<>();
    private transient boolean restoring;
    public HotSeatPolicies() {
        for (var param : params())
            if (!param.isSubMenu() && !param.getCfgLabel().isBlank())
                values.put(param.getCfgLabel(), param.getCfgValue());
    }
    public boolean locks(String key) { return !restoring && values.containsKey(key); }
    public void restore() {
        restoring = true;
        try {
            for (var param : params()) {
                String value = values.get(param.getCfgLabel());
                if (value != null) param.setFromCfgValue(value);
            }
        } finally { restoring = false; }
    }

    /** Settings stored per computer that change simulation; every seat must resolve with the same values. */
    private static List<IParam<?>> params() {
        List<IParam<?>> params = new ArrayList<>(AllSubUI.governorSubUI().optionsList());
        params.add(IInGameOptions.rallyCombat);
        params.add(IInGameOptions.rallyCombatLoss);
        params.add(IInGameOptions.gameAgressiveness);
        params.add(IMainOptions.shipBasedMissiles);
        params.add(ShipCombatManager.playerDontTargetHarmlessColony);
        return params;
    }
}
