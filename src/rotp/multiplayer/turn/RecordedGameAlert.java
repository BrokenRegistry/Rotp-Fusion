package rotp.multiplayer.turn;

import java.util.Objects;
import rotp.ui.notifications.GameAlert;

/** Desktop-compatible view of a persisted recipient alert. */
public final class RecordedGameAlert extends GameAlert {
    private final AlertRecord record;

    public RecordedGameAlert(AlertRecord record) {
        this.record = Objects.requireNonNull(record, "record");
    }

    @Override public String description() { return record.description(); }
    @Override public int sysId() { return record.systemId(); }
}
