public class SecurityPanel {
    public int col, row;
    public float progress; // 0-100
    public MiniGame currentMiniGame;

    public SecurityPanel(int col, int row) {
        this.col = col;
        this.row = row;
        this.progress = 0f;
        this.currentMiniGame = null;
    }

    public boolean isDone() {
        return progress >= 100f;
    }

    public void assignRandomMiniGame() {
        int type = (int)(Math.random() * 4);
        switch (type) {
            case 0: currentMiniGame = new MathMiniGame(this); break;
            case 1: currentMiniGame = new AlternatingMiniGame(this); break;
            case 2: currentMiniGame = new TimingMiniGame(this); break;
            case 3: currentMiniGame = new TypingMiniGame(this); break;
            default: currentMiniGame = new MathMiniGame(this); break;
        }
    }
}
