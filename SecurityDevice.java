public class SecurityDevice {
    public static final int TYPE_MATH = 0;
    public static final int TYPE_ZC_ALT = 1;
    public static final int TYPE_TIMING = 2;
    public static final int TYPE_TYPING = 3;

    public int x, y;
    public int type;
    public boolean cleared;
    public int tileIndex; // index into GameMap device tiles (0-5)

    public SecurityDevice(int x, int y, int type, int tileIndex) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.cleared = false;
        this.tileIndex = tileIndex;
    }
}
