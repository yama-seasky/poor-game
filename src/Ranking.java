import java.io.*;
import java.util.*;

public class Ranking {
    public static class Entry {
        public String name;
        public long timeMs;
        public Entry(String name, long timeMs) {
            this.name = name;
            this.timeMs = timeMs;
        }
    }

    private static final String FILE = "ranking.dat";
    private static final int MAX = 5;
    public List<Entry> entries = new ArrayList<>();

    public Ranking() {
        load();
    }

    private void load() {
        entries.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    entries.add(new Entry(parts[0], Long.parseLong(parts[1])));
                }
            }
        } catch (Exception e) {
            // no file yet
        }
        entries.sort(Comparator.comparingLong(e -> e.timeMs));
    }

    public boolean isTopFive(long timeMs) {
        if (entries.size() < MAX) return true;
        return timeMs < entries.get(entries.size() - 1).timeMs;
    }

    public void addEntry(String name, long timeMs) {
        entries.add(new Entry(name, timeMs));
        entries.sort(Comparator.comparingLong(e -> e.timeMs));
        if (entries.size() > MAX) entries = entries.subList(0, MAX);
        save();
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Entry e : entries) {
                pw.println(e.name + ":" + e.timeMs);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public String formatTime(long ms) {
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        long millis = ms % 1000;
        return String.format("%d:%02d.%03d", min, sec, millis);
    }
}
