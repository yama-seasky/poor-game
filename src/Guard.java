public class Guard {
    public int x, y;
    public String direction;
    public boolean horizontal; // true=horizontal, false=vertical
    public int patrolMin, patrolMax;
    public long intervalMs;
    private long timeSinceMove = 0;

    public Guard(int x, int y, boolean horizontal, int patrolMin, int patrolMax, long intervalMs, String direction) {
        this.x = x;
        this.y = y;
        this.horizontal = horizontal;
        this.patrolMin = patrolMin;
        this.patrolMax = patrolMax;
        this.intervalMs = intervalMs;
        this.direction = direction;
    }

    public void update(long dt) {
        timeSinceMove += dt;
        if (timeSinceMove >= intervalMs) {
            timeSinceMove -= intervalMs;
            move();
        }
    }

    private void move() {
        if (horizontal) {
            if (direction.equals("RIGHT")) {
                x++;
                if (x > patrolMax) { x = patrolMax; direction = "LEFT"; }
            } else {
                x--;
                if (x < patrolMin) { x = patrolMin; direction = "RIGHT"; }
            }
        } else {
            if (direction.equals("DOWN")) {
                y++;
                if (y > patrolMax) { y = patrolMax; direction = "UP"; }
            } else {
                y--;
                if (y < patrolMin) { y = patrolMin; direction = "DOWN"; }
            }
        }
    }

    public char getDirectionChar() {
        switch (direction) {
            case "UP":    return '∧';
            case "DOWN":  return 'Ｖ';
            case "LEFT":  return '＜';
            case "RIGHT": return '＞';
            default:      return 'Ｖ';
        }
    }

    public boolean detectsPlayer(int px, int py) {
        // player center = (px, py+1)
        int pcx = px, pcy = py + 1;
        int gcx = x, gcy = y + 1;
        int RANGE = 6, SIDE = 2;

        switch (direction) {
            case "RIGHT":
                return pcx >= gcx + 1 && pcx <= gcx + RANGE && Math.abs(pcy - gcy) <= SIDE;
            case "LEFT":
                return pcx <= gcx - 1 && pcx >= gcx - RANGE && Math.abs(pcy - gcy) <= SIDE;
            case "DOWN":
                return pcy >= gcy + 2 && pcy <= gcy + 1 + RANGE && Math.abs(pcx - gcx) <= SIDE;
            case "UP":
                return pcy <= gcy - 1 && pcy >= gcy - RANGE && Math.abs(pcx - gcx) <= SIDE;
        }
        return false;
    }
}
