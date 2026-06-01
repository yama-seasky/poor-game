import java.util.*;

public class GameState {
    public enum GamePhase {
        START_MENU, HOW_TO_PLAY, RANKING, PLAYING, PICKING_LOCK, GIMMICK, NAME_INPUT, GAME_OVER, GAME_CLEAR, EXITING
    }

    public GamePhase phase = GamePhase.START_MENU;
    public MapData map;
    public Player player;
    public Guard[] guards;
    public SecurityPanel[] panels;
    public long gameTimeMs = 0;
    public float pickingProgress = 0f;

    // Menu state
    public int menuIndex = 0;
    public static final String[] MENU_ITEMS = {"ゲーム開始", "ランキング", "遊び方", "終了"};

    // Game over menu
    public int gameOverMenuIndex = 0;
    public static final String[] GAMEOVER_MENU = {"リトライ", "タイトルに戻る"};

    // Game clear menu
    public int gameClearMenuIndex = 0;
    public static final String[] GAMECLEAR_MENU = {"リトライ", "タイトルに戻る"};

    // Name input
    public String inputName = "";
    public boolean nameSubmitted = false;

    // Active gimmick panel index
    public int activePanel = -1;

    // Ranking
    public Ranking ranking = new Ranking();

    public GameState() {
        initGame();
    }

    public void initGame() {
        map = new MapData();
        player = new Player(4, 5);
        guards = new Guard[] {
            new Guard(25,  3,  true,  25, 118, 400, "RIGHT"),
            new Guard(25,  51, true,  25, 118, 450, "RIGHT"),
            new Guard(48,  7,  false, 7,  47,  500, "DOWN"),
            new Guard(73,  7,  false, 7,  47,  480, "DOWN"),
            new Guard(25,  27, true,  25, 118, 420, "RIGHT"),
        };
        panels = new SecurityPanel[6];
        int[][] pp = MapData.PANEL_POS;
        for (int i = 0; i < 6; i++) {
            panels[i] = new SecurityPanel(pp[i][0], pp[i][1]);
        }
        gameTimeMs = 0;
        pickingProgress = 0f;
        activePanel = -1;
        phase = GamePhase.PLAYING;
        gameOverMenuIndex = 0;
        gameClearMenuIndex = 0;
        inputName = "";
        nameSubmitted = false;
    }

    public void update(long dt) {
        if (phase == GamePhase.PLAYING || phase == GamePhase.GIMMICK || phase == GamePhase.PICKING_LOCK) {
            gameTimeMs += dt;
        }
        if (phase == GamePhase.PICKING_LOCK) {
            pickingProgress -= 3f * dt / 1000f;
            if (pickingProgress < 0f) pickingProgress = 0f;
        }
        if (phase == GamePhase.PLAYING) {
            for (Guard g : guards) g.update(dt);
            checkDetection();
        }
        if (phase == GamePhase.GIMMICK && activePanel >= 0) {
            panels[activePanel].currentMiniGame.update(dt);
            if (panels[activePanel].currentMiniGame.isDone()) {
                panels[activePanel].progress = 100f;
                map.setTile(panels[activePanel].col, panels[activePanel].row, MapData.SECURITY_PANEL_DONE);
                checkGateOpen();
                // stay in gimmick so player sees completion, exit via ESC
            }
        }
    }

    private void checkDetection() {
        for (Guard g : guards) {
            if (g.detectsPlayer(player.x, player.y)) {
                phase = GamePhase.GAME_OVER;
                return;
            }
        }
    }

    public int countPanelsDone() {
        int count = 0;
        for (SecurityPanel p : panels) if (p.isDone()) count++;
        return count;
    }

    public void checkGateOpen() {
        if (countPanelsDone() >= 4) {
            map.setTile(MapData.GATE_COL, MapData.GATE_ROW, MapData.GATE_OPEN);
        }
    }
}
