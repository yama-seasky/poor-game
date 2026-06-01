public class Player {
    public int x, y;
    public String direction; // "UP","DOWN","LEFT","RIGHT"

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = "DOWN";
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
}
