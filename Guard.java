public class Guard {
    public int x, y;
    public int direction; // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    public int[][] waypoints;
    public int waypointIndex;
    public boolean forward; // for back-and-forth patrol

    public Guard(int startX, int startY, int[][] waypoints) {
        this.x = startX;
        this.y = startY;
        this.waypoints = waypoints;
        this.waypointIndex = 0;
        this.forward = true;
        this.direction = DIR_RIGHT;
    }

    // Move toward next waypoint
    public void update(GameMap map) {
        if (waypoints == null || waypoints.length == 0) return;
        int[] target = waypoints[waypointIndex];
        int tx = target[0];
        int ty = target[1];

        if (x == tx && y == ty) {
            // Reached waypoint, advance
            waypointIndex = (waypointIndex + 1) % waypoints.length;
            target = waypoints[waypointIndex];
            tx = target[0];
            ty = target[1];
        }

        // Move one step toward target
        int dx = tx - x;
        int dy = ty - y;

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx > 0) {
                if (map.isWalkable(x + 1, y) || isGateTile(map, x + 1, y)) { x++; direction = DIR_RIGHT; }
            } else if (dx < 0) {
                if (map.isWalkable(x - 1, y) || isGateTile(map, x - 1, y)) { x--; direction = DIR_LEFT; }
            }
        } else {
            if (dy > 0) {
                if (map.isWalkable(x, y + 1) || isGateTile(map, x, y + 1)) { y++; direction = DIR_DOWN; }
            } else if (dy < 0) {
                if (map.isWalkable(x, y - 1) || isGateTile(map, x, y - 1)) { y--; direction = DIR_UP; }
            }
        }
    }

    private boolean isGateTile(GameMap map, int gx, int gy) {
        if (gx < 0 || gx >= GameMap.MAP_W || gy < 0 || gy >= GameMap.MAP_H) return false;
        int t = map.tiles[gy][gx];
        return t == GameMap.GATE_CLOSED || t == GameMap.GATE_OPEN;
    }

    // Check if a player position is in this guard's detection zone
    // Detection zone: rectangle 5 tiles deep, 3 tiles wide centered on guard, in front direction
    // Returns true if detected (and no wall blocking)
    public boolean detectsPlayer(int px, int py, GameMap map) {
        int zoneX1, zoneX2, zoneY1, zoneY2;

        switch (direction) {
            case DIR_UP:
                zoneX1 = x - 1; zoneX2 = x + 1;
                zoneY1 = y - 5; zoneY2 = y - 1;
                break;
            case DIR_DOWN:
                zoneX1 = x - 1; zoneX2 = x + 1;
                zoneY1 = y + 1; zoneY2 = y + 5;
                break;
            case DIR_LEFT:
                zoneX1 = x - 5; zoneX2 = x - 1;
                zoneY1 = y - 1; zoneY2 = y + 1;
                break;
            case DIR_RIGHT:
            default:
                zoneX1 = x + 1; zoneX2 = x + 5;
                zoneY1 = y - 1; zoneY2 = y + 1;
                break;
        }

        if (px < zoneX1 || px > zoneX2 || py < zoneY1 || py > zoneY2) return false;

        // Line of sight check
        return hasLineOfSight(px, py, map);
    }

    private boolean hasLineOfSight(int px, int py, GameMap map) {
        // Check along axis from guard to player
        int dx = px - x;
        int dy = py - y;

        if (dx == 0) {
            // Same column, check vertical
            int step = dy > 0 ? 1 : -1;
            for (int cy = y + step; cy != py; cy += step) {
                if (map.isWall(x, cy)) return false;
            }
        } else if (dy == 0) {
            // Same row, check horizontal
            int step = dx > 0 ? 1 : -1;
            for (int cx = x + step; cx != px; cx += step) {
                if (map.isWall(cx, y)) return false;
            }
        } else {
            // Diagonal - use simple check along dominant axis
            // Check tiles along path
            int stepX = dx > 0 ? 1 : -1;
            int stepY = dy > 0 ? 1 : -1;
            int cx = x + stepX;
            int cy = y + stepY;
            while (cx != px || cy != py) {
                if (map.isWall(cx, cy)) return false;
                if (cx != px) cx += stepX;
                if (cy != py) cy += stepY;
            }
        }
        return true;
    }

    // Get detection zone bounds for rendering
    public int[] getDetectionZone() {
        switch (direction) {
            case DIR_UP:
                return new int[]{x - 1, y - 5, x + 1, y - 1};
            case DIR_DOWN:
                return new int[]{x - 1, y + 1, x + 1, y + 5};
            case DIR_LEFT:
                return new int[]{x - 5, y - 1, x - 1, y + 1};
            case DIR_RIGHT:
            default:
                return new int[]{x + 1, y - 1, x + 5, y + 1};
        }
    }
}
