public class MapData {
    public static final int MAP_W = 155;
    public static final int MAP_H = 55;

    public static final char WALL                  = '■';
    public static final char FLOOR                 = '　'; // ideographic space
    public static final char LOCKED_DOOR           = '錠';
    public static final char OPEN_DOOR             = '口';
    public static final char SECURITY_PANEL_ACTIVE = '装';
    public static final char SECURITY_PANEL_DONE   = '済';
    public static final char GATE_CLOSED           = '門';
    public static final char GATE_OPEN             = '　';
    public static final char EXIT                  = '出';

    private char[][] tiles = new char[MAP_H][MAP_W];

    // Panel positions
    public static final int[][] PANEL_POS = {
        {48, 3},
        {73, 3},
        {98, 3},
        {48, 51},
        {73, 51},
        {98, 51}
    };

    // Gate position
    public static final int GATE_COL = 126;
    public static final int GATE_ROW = 27;

    public MapData() {
        buildMap();
    }

    private void buildMap() {
        // 1. Fill entire map with WALL
        for (int r = 0; r < MAP_H; r++)
            for (int c = 0; c < MAP_W; c++)
                tiles[r][c] = WALL;

        // 2. Prison cell interior: rows 2-12, cols 2-12 → FLOOR
        for (int r = 2; r <= 12; r++)
            for (int c = 2; c <= 12; c++)
                tiles[r][c] = FLOOR;

        // 3. col 13, row 7 = LOCKED_DOOR (rest of col 13 rows 1-13 stays WALL)
        tiles[7][13] = LOCKED_DOOR;

        // 4. Connection corridor: rows 5-9, cols 14-21 → FLOOR
        for (int r = 5; r <= 9; r++)
            for (int c = 14; c <= 21; c++)
                tiles[r][c] = FLOOR;

        // 5. Main horizontal corridors (5 rows each)
        // H_top: rows 1-5, cols 21-130
        for (int r = 1; r <= 5; r++)
            for (int c = 21; c <= 130; c++)
                tiles[r][c] = FLOOR;
        // H_mid: rows 25-29, cols 21-130
        for (int r = 25; r <= 29; r++)
            for (int c = 21; c <= 130; c++)
                tiles[r][c] = FLOOR;
        // H_bot: rows 49-53, cols 21-130
        for (int r = 49; r <= 53; r++)
            for (int c = 21; c <= 130; c++)
                tiles[r][c] = FLOOR;

        // 6. Main vertical corridors (5 cols each)
        // V_left: cols 21-25, rows 1-53
        for (int r = 1; r <= 53; r++)
            for (int c = 21; c <= 25; c++)
                tiles[r][c] = FLOOR;
        // V_ml: cols 46-50, rows 1-53
        for (int r = 1; r <= 53; r++)
            for (int c = 46; c <= 50; c++)
                tiles[r][c] = FLOOR;
        // V_ctr: cols 71-75, rows 1-53
        for (int r = 1; r <= 53; r++)
            for (int c = 71; c <= 75; c++)
                tiles[r][c] = FLOOR;
        // V_mr: cols 96-100, rows 1-53
        for (int r = 1; r <= 53; r++)
            for (int c = 96; c <= 100; c++)
                tiles[r][c] = FLOOR;
        // V_right: cols 121-125, rows 1-53
        for (int r = 1; r <= 53; r++)
            for (int c = 121; c <= 125; c++)
                tiles[r][c] = FLOOR;

        // 7. Exit corridor: rows 25-29, cols 126-153 → FLOOR
        for (int r = 25; r <= 29; r++)
            for (int c = 126; c <= 153; c++)
                tiles[r][c] = FLOOR;

        // 8. EXIT tile: row 27, col 153
        tiles[27][153] = EXIT;

        // 9. GATE tile: row 27, col 126 = GATE_CLOSED
        tiles[27][126] = GATE_CLOSED;

        // Security panels
        for (int[] p : PANEL_POS) {
            tiles[p[1]][p[0]] = SECURITY_PANEL_ACTIVE;
        }
    }

    public char getTile(int col, int row) {
        if (col < 0 || col >= MAP_W || row < 0 || row >= MAP_H) return WALL;
        return tiles[row][col];
    }

    public void setTile(int col, int row, char tile) {
        if (col < 0 || col >= MAP_W || row < 0 || row >= MAP_H) return;
        tiles[row][col] = tile;
    }

    public boolean isPassable(int col, int row) {
        char t = getTile(col, row);
        return t == FLOOR || t == OPEN_DOOR || t == GATE_OPEN || t == EXIT
            || t == SECURITY_PANEL_ACTIVE || t == SECURITY_PANEL_DONE;
    }
}
