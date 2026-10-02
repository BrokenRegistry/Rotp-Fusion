package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HotSeatInbox implements Serializable {
    private static final long serialVersionUID = 1L;
    private long nextId = 1;
    private final List<HotSeatReport> reports = new ArrayList<>();

    public long append(int turn, int owner, String kind, String title, List<String> lines) {
        return append(turn, owner, kind, title, lines, null);
    }
    public synchronized long append(int turn, int owner, String kind, String title,
            List<String> lines, java.io.Serializable payload) {
        if (turn < 0 || owner < 0) throw new IllegalArgumentException("Invalid report owner or turn");
        var report = new HotSeatReport(nextId, turn, owner, Objects.requireNonNull(kind),
                Objects.requireNonNull(title), lines, payload);
        reports.add(report);
        return nextId++;
    }

    public synchronized List<HotSeatReport> unread(int owner) {
        return reports.stream().filter(r -> r.recipientEmpireId() == owner).toList();
    }

    public synchronized boolean acknowledge(int owner, long reportId) {
        return reports.removeIf(r -> r.recipientEmpireId() == owner && r.id() == reportId);
    }
}
