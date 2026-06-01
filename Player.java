public class Player {
    public int x, y;
    public int direction; // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.direction = DIR_DOWN;
    }
}
