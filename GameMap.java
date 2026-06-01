public class GameMap {
    public static final int WALL = 0;
    public static final int FLOOR = 1;
    public static final int JAIL_FLOOR = 2;
    public static final int DOOR_LOCKED = 3;
    public static final int DOOR_OPEN = 4;
    public static final int GATE_CLOSED = 5;
    public static final int GATE_OPEN = 6;
    public static final int EXIT = 7;
    public static final int DEVICE_BASE = 10;

    public static final int MAP_W = 150;
    public static final int MAP_H = 80;

    public int[][] tiles;

    public GameMap() {
        tiles = new int[MAP_H][MAP_W];
        generate();
    }

    private void generate() {
        // Fill all with WALL
        for (int y = 0; y < MAP_H; y++) {
            for (int x = 0; x < MAP_W; x++) {
                tiles[y][x] = WALL;
            }
        }

        // Border walls already set by WALL fill
        // JAIL CELL: walls at rect x=2-23, y=34-56; floor inside x=3-22, y=35-55
        for (int y = 34; y <= 56; y++) {
            for (int x = 2; x <= 23; x++) {
                tiles[y][x] = WALL;
            }
        }
        for (int y = 35; y <= 55; y++) {
            for (int x = 3; x <= 22; x++) {
                tiles[y][x] = JAIL_FLOOR;
            }
        }
        // DOOR_LOCKED at tile (23,44)
        tiles[44][23] = DOOR_LOCKED;

        // MAIN PRISON: FLOOR fills x=24-148, y=2-77
        for (int y = 2; y <= 77; y++) {
            for (int x = 24; x <= 148; x++) {
                tiles[y][x] = FLOOR;
            }
        }

        // Internal dividing walls
        // Horizontal wall y=25, x=24-55 (gap at x=38-42)
        for (int x = 24; x <= 55; x++) {
            if (x < 38 || x > 42) tiles[25][x] = WALL;
        }
        // Horizontal wall y=25, x=65-85 (gap at x=74-76)
        for (int x = 65; x <= 85; x++) {
            if (x < 74 || x > 76) tiles[25][x] = WALL;
        }
        // Horizontal wall y=25, x=90-120 (gap at x=104-106)
        for (int x = 90; x <= 120; x++) {
            if (x < 104 || x > 106) tiles[25][x] = WALL;
        }
        // Horizontal wall y=55, x=24-55 (gap at x=38-42)
        for (int x = 24; x <= 55; x++) {
            if (x < 38 || x > 42) tiles[55][x] = WALL;
        }
        // Horizontal wall y=55, x=65-85 (gap at x=74-76)
        for (int x = 65; x <= 85; x++) {
            if (x < 74 || x > 76) tiles[55][x] = WALL;
        }
        // Horizontal wall y=55, x=90-120 (gap at x=104-106)
        for (int x = 90; x <= 120; x++) {
            if (x < 104 || x > 106) tiles[55][x] = WALL;
        }
        // Vertical wall x=62, y=2-24 (gap at y=12-14)
        for (int y = 2; y <= 24; y++) {
            if (y < 12 || y > 14) tiles[y][62] = WALL;
        }
        // Vertical wall x=62, y=56-77 (gap at y=66-68)
        for (int y = 56; y <= 77; y++) {
            if (y < 66 || y > 68) tiles[y][62] = WALL;
        }
        // Vertical wall x=88, y=2-24 (gap at y=12-14)
        for (int y = 2; y <= 24; y++) {
            if (y < 12 || y > 14) tiles[y][88] = WALL;
        }
        // Vertical wall x=88, y=56-77 (gap at y=66-68)
        for (int y = 56; y <= 77; y++) {
            if (y < 66 || y > 68) tiles[y][88] = WALL;
        }

        // SECURITY DEVICES
        tiles[12][38] = DEVICE_BASE + 0;
        tiles[67][38] = DEVICE_BASE + 1;
        tiles[12][74] = DEVICE_BASE + 2;
        tiles[67][74] = DEVICE_BASE + 3;
        tiles[12][104] = DEVICE_BASE + 4;
        tiles[67][104] = DEVICE_BASE + 5;

        // GATE (closed): tiles (128, 37-43)
        for (int y = 37; y <= 43; y++) {
            tiles[y][128] = GATE_CLOSED;
        }

        // EXIT: tile (147, 40)
        tiles[40][147] = EXIT;
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= MAP_W || y < 0 || y >= MAP_H) return false;
        int t = tiles[y][x];
        return t == FLOOR || t == JAIL_FLOOR || t == DOOR_OPEN || t == GATE_OPEN || t == EXIT ||
               (t >= DEVICE_BASE && t <= DEVICE_BASE + 5);
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= MAP_W || y < 0 || y >= MAP_H) return true;
        return tiles[y][x] == WALL;
    }

    public void openGate() {
        for (int y = 37; y <= 43; y++) {
            if (tiles[y][128] == GATE_CLOSED) {
                tiles[y][128] = GATE_OPEN;
            }
        }
    }

    public void clearDevice(int deviceIndex) {
        // Find the device tile and mark it cleared (DEVICE_BASE + 6 + index or use a separate marker)
        // We'll use negative values to indicate cleared: store as -(DEVICE_BASE + index)
        // Actually let's use a simpler approach: cleared devices are stored in SecurityDevice
    }
}
