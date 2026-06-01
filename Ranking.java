import java.io.*;
import java.util.*;

public class Ranking {
    public static final int MAX_ENTRIES = 5;
    public static final String FILE_NAME = "ranking.dat";

    public static class Entry implements Comparable<Entry> {
        public String name;
        public int seconds;
        public Entry(String name, int seconds) {
            this.name = name;
            this.seconds = seconds;
        }
        @Override
        public int compareTo(Entry o) {
            return Integer.compare(this.seconds, o.seconds);
        }
    }

    private List<Entry> entries;

    public Ranking() {
        entries = new ArrayList<>();
        load();
    }

    public void load() {
        entries.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    String name = line.substring(0, colon);
                    int secs = Integer.parseInt(line.substring(colon + 1).trim());
                    entries.add(new Entry(name, secs));
                }
            }
        } catch (Exception e) {
            // No file yet
        }
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) entries = entries.subList(0, MAX_ENTRIES);
    }

    public boolean qualifies(int seconds) {
        if (entries.size() < MAX_ENTRIES) return true;
        return seconds < entries.get(entries.size() - 1).seconds;
    }

    public void addEntry(String name, int seconds) {
        entries.add(new Entry(name, seconds));
        Collections.sort(entries);
        if (entries.size() > MAX_ENTRIES) entries = new ArrayList<>(entries.subList(0, MAX_ENTRIES));
        save();
    }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Entry e : entries) {
                pw.println(e.name + ":" + e.seconds);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
