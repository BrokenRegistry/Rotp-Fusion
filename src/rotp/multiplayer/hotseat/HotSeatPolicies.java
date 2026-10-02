package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import rotp.ui.options.AllSubUI;

/** Match-wide governor defaults are immutable during this local milestone. */
public final class HotSeatPolicies implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, String> values = new LinkedHashMap<>();
    private transient boolean restoring;
    public HotSeatPolicies() {
        for (var param : AllSubUI.governorSubUI().optionsList())
            if (!param.isSubMenu() && !param.getCfgLabel().isBlank())
                values.put(param.getCfgLabel(), param.getCfgValue());
    }
    public boolean locks(String key) { return !restoring && values.containsKey(key); }
    public void restore() {
        restoring = true;
        try {
            for (var param : AllSubUI.governorSubUI().optionsList()) {
                String value = values.get(param.getCfgLabel());
                if (value != null) param.setFromCfgValue(value);
            }
        } finally { restoring = false; }
    }
}
