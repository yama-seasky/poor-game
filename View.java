import java.util.*;

public class View {
    private Model model;

    // ANSI codes
    static final String RESET = "\033[0m";
    static final String BOLD = "\033[1m";
    static final String RED = "\033[31m";
    static final String GREEN = "\033[32m";
    static final String YELLOW = "\033[33m";
    static final String BLUE = "\033[34m";
    static final String MAGENTA = "\033[35m";
    static final String CYAN = "\033[36m";
    static final String WHITE = "\033[37m";
    static final String BRIGHT_RED = "\033[91m";
    static final String BRIGHT_GREEN = "\033[92m";
    static final String BRIGHT_YELLOW = "\033[93m";
    static final String BRIGHT_CYAN = "\033[96m";
    static final String BG_BLACK = "\033[40m";
    static final String BG_RED = "\033[41m";
    static final String BG_BLUE = "\033[44m";
    static final String BG_MAGENTA = "\033[45m";
    static final String BG_WHITE = "\033[47m";
    static final String REVERSE = "\033[7m";
    static final String BG_DARK_GRAY = "\033[100m";
    static final String BG_GREEN = "\033[42m";

    static final int VIEW_W = 100; // tiles wide
    static final int VIEW_H = 46;  // tiles tall
    static final int SCREEN_ROWS = 50;

    public View(Model model) {
        this.model = model;
    }

    public void render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[H"); // move cursor home

        switch (model.state) {
            case Model.STATE_TITLE:
                renderTitle(sb);
                break;
            case Model.STATE_HOW_TO_PLAY:
                renderHowToPlay(sb);
                break;
            case Model.STATE_RANKING:
                renderRanking(sb);
                break;
            case Model.STATE_PICKING_LOCK:
                renderGame(sb);
                renderPickingLockOverlay(sb);
                break;
            case Model.STATE_PLAYING:
                renderGame(sb);
                break;
            case Model.STATE_MINIGAME:
                renderGame(sb);
                renderMinigameOverlay(sb);
                break;
            case Model.STATE_GAME_OVER:
                renderGameOver(sb);
                break;
            case Model.STATE_GAME_CLEAR:
                renderGameClear(sb);
                break;
        }

        model.out.print(sb.toString());
        model.out.flush();
    }

    // ==================== TITLE ====================
    private void renderTitle(StringBuilder sb) {
        clearScreen(sb);
        // Title centered
        String titleLine = "Ｆｏｒｔｒｅｓｓ　ｏｆ　Ｄｅｓｐａｉｒ";
        centerLine(sb, BOLD + BRIGHT_YELLOW + titleLine + RESET, 6);
        centerLineRaw(sb, BRIGHT_CYAN + "＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊＊" + RESET, 8);

        String[] menu = {"ゲーム開始", "ランキング", "遊び方", "終了"};
        for (int i = 0; i < menu.length; i++) {
            String prefix = (i == model.titleSelection) ? REVERSE + BRIGHT_YELLOW : WHITE;
            centerLineRaw(sb, prefix + "　" + menu[i] + "　" + RESET, 14 + i * 3);
        }

        centerLineRaw(sb, CYAN + "ＷＳキーもしくは上下キーで選択、Ｅｎｔｅｒで決定" + RESET, 28);

        // Fill remaining rows
        fillToRow(sb, 50);
    }

    // ==================== HOW TO PLAY ====================
    private void renderHowToPlay(StringBuilder sb) {
        clearScreen(sb);
        centerLineRaw(sb, BOLD + BRIGHT_YELLOW + "　遊　び　方　" + RESET, 3);
        printLine(sb, CYAN + "　移動：　ＷＡＳＤ　または　矢印キー" + RESET, 6);
        printLine(sb, CYAN + "　ピッキング：　スペースキーを連打" + RESET, 8);
        printLine(sb, CYAN + "　保安装置解除：　装置に隣接してＥキー" + RESET, 10);
        printLine(sb, CYAN + "　ミニゲームキャンセル：　ＥＳＣキー" + RESET, 12);
        printLine(sb, WHITE + "　目標：　保安装置を４つ以上解除し、ゲートを開けて脱出せよ！" + RESET, 15);
        printLine(sb, YELLOW + "　警備員の視野（赤）に入るとゲームオーバー！" + RESET, 18);
        centerLineRaw(sb, GREEN + "Ｅｎｔｅｒキーでタイトルに戻る" + RESET, 22);
        fillToRow(sb, 50);
    }

    // ==================== RANKING ====================
    private void renderRanking(StringBuilder sb) {
        clearScreen(sb);
        centerLineRaw(sb, BOLD + BRIGHT_YELLOW + "　ラ　ン　キ　ン　グ　" + RESET, 3);
        List<Ranking.Entry> entries = model.ranking.getEntries();
        if (entries.isEmpty()) {
            centerLineRaw(sb, WHITE + "まだ記録がありません" + RESET, 8);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                Ranking.Entry e = entries.get(i);
                int mm = e.seconds / 60;
                int ss = e.seconds % 60;
                String timeStr = String.format("%02d分%02d秒", mm, ss);
                String rank = (i + 1) + "位";
                centerLineRaw(sb, CYAN + rank + "　" + e.name + "　" + timeStr + RESET, 8 + i * 3);
            }
        }
        centerLineRaw(sb, GREEN + "Ｅｎｔｅｒキーでタイトルに戻る" + RESET, 26);
        fillToRow(sb, 50);
    }

    // ==================== GAME RENDERING ====================
    private void renderGame(StringBuilder sb) {
        GameMap map = model.map;
        Player player = model.player;

        // Camera
        int camX = Math.max(0, Math.min(player.x - VIEW_W / 2, GameMap.MAP_W - VIEW_W));
        int camY = Math.max(0, Math.min(player.y - VIEW_H / 2, GameMap.MAP_H - VIEW_H));

        // Build detection zone set for rendering
        Set<Long> detectionTiles = new HashSet<>();
        for (Guard g : model.guards) {
            int[] zone = g.getDetectionZone();
            int x1 = zone[0], y1 = zone[1], x2 = zone[2], y2 = zone[3];
            for (int gy = y1; gy <= y2; gy++) {
                for (int gx = x1; gx <= x2; gx++) {
                    if (gx >= 0 && gx < GameMap.MAP_W && gy >= 0 && gy < GameMap.MAP_H) {
                        if (!map.isWall(gx, gy)) {
                            detectionTiles.add((long)gy * GameMap.MAP_W + gx);
                        }
                    }
                }
            }
        }

        // Build entity position maps
        // player positions: top=y-1, mid=y, bot=y+1
        Map<Long, String> entityChars = new HashMap<>();
        // Guards
        for (Guard g : model.guards) {
            String top = RED + "看" + RESET;
            String mid = RED + guardMidChar(g.direction) + RESET;
            String bot = RED + "Ａ" + RESET;
            putEntityChar(entityChars, g.x, g.y - 1, top);
            putEntityChar(entityChars, g.x, g.y, mid);
            putEntityChar(entityChars, g.x, g.y + 1, bot);
        }
        // Player (overwrites guard if same position - shouldn't happen)
        {
            String top = CYAN + "＠" + RESET;
            String mid = CYAN + playerMidChar(player.direction) + RESET;
            String bot = CYAN + "Ａ" + RESET;
            putEntityChar(entityChars, player.x, player.y - 1, top);
            putEntityChar(entityChars, player.x, player.y, mid);
            putEntityChar(entityChars, player.x, player.y + 1, bot);
        }

        // Render VIEW_H rows
        for (int row = 0; row < VIEW_H; row++) {
            int mapY = camY + row;
            for (int col = 0; col < VIEW_W; col++) {
                int mapX = camX + col;
                long key = (long)mapY * GameMap.MAP_W + mapX;

                String entityChar = entityChars.get(key);
                if (entityChar != null) {
                    // Check if in detection zone - add bg
                    if (detectionTiles.contains(key)) {
                        sb.append(BG_RED);
                    }
                    sb.append(entityChar);
                } else {
                    boolean inDetection = detectionTiles.contains(key);
                    sb.append(renderTile(map, mapX, mapY, inDetection));
                }
            }
            sb.append(RESET).append("\n");
        }

        // HUD - 4 rows
        renderHUD(sb);
    }

    private void putEntityChar(Map<Long, String> map, int x, int y, String ch) {
        if (x >= 0 && x < GameMap.MAP_W && y >= 0 && y < GameMap.MAP_H) {
            map.put((long)y * GameMap.MAP_W + x, ch);
        }
    }

    private String guardMidChar(int dir) {
        switch (dir) {
            case Guard.DIR_UP: return "∧";
            case Guard.DIR_DOWN: return "Ｖ";
            case Guard.DIR_LEFT: return "＜";
            case Guard.DIR_RIGHT: default: return "＞";
        }
    }

    private String playerMidChar(int dir) {
        switch (dir) {
            case Player.DIR_UP: return "∧";
            case Player.DIR_DOWN: return "Ｖ";
            case Player.DIR_LEFT: return "＜";
            case Player.DIR_RIGHT: default: return "＞";
        }
    }

    private String renderTile(GameMap map, int x, int y, boolean inDetection) {
        if (x < 0 || x >= GameMap.MAP_W || y < 0 || y >= GameMap.MAP_H) {
            return inDetection ? BG_RED + WHITE + "■" + RESET : WHITE + "■" + RESET;
        }
        int t = map.tiles[y][x];
        String bg = inDetection ? BG_RED : "";
        switch (t) {
            case GameMap.WALL:
                return WHITE + "■" + RESET;
            case GameMap.FLOOR:
                return bg + BG_BLACK + "　" + RESET;
            case GameMap.JAIL_FLOOR:
                return bg.isEmpty() ? BG_DARK_GRAY + "　" + RESET : BG_RED + "　" + RESET;
            case GameMap.DOOR_LOCKED:
                return bg + YELLOW + "錠" + RESET;
            case GameMap.DOOR_OPEN:
                return bg + BG_BLACK + "　" + RESET;
            case GameMap.GATE_CLOSED:
                return bg + YELLOW + "門" + RESET;
            case GameMap.GATE_OPEN:
                return bg + BG_BLACK + "　" + RESET;
            case GameMap.EXIT:
                return bg + BRIGHT_GREEN + "出" + RESET;
            default:
                if (t >= GameMap.DEVICE_BASE && t < GameMap.DEVICE_BASE + 6) {
                    return bg + RED + "装" + RESET;
                } else if (t >= GameMap.DEVICE_BASE + 6 && t < GameMap.DEVICE_BASE + 12) {
                    return bg + GREEN + "済" + RESET;
                }
                return bg + BG_BLACK + "　" + RESET;
        }
    }

    private void renderHUD(StringBuilder sb) {
        int mm = model.elapsedSeconds / 60;
        int ss = model.elapsedSeconds % 60;
        String timeStr = String.format("%02d:%02d", mm, ss);
        int cleared = model.devicesCleared;

        // Row 47 (index 46)
        sb.append(RESET + BG_BLACK + WHITE);
        sb.append(String.format("　時間：%s　　保安装置：%d/4解除　　", timeStr, cleared));
        // Pad to 100 full-width chars
        int len1 = 19; // approximate
        for (int i = len1; i < VIEW_W; i++) sb.append("　");
        sb.append(RESET).append("\n");

        // Row 48 - contextual message
        sb.append(RESET + BG_BLACK + BRIGHT_CYAN);
        String msg = model.hudMessage.isEmpty() ? "" : model.hudMessage;
        sb.append(msg);
        int msgLen = msg.length() / 3; // rough estimate in full-width chars (each full-width = 3 bytes in java string but counted as 1 char)
        // Better: just pad
        for (int i = 0; i < VIEW_W; i++) sb.append("　");
        sb.append(RESET).append("\n");

        // Row 49 - empty
        sb.append(RESET + BG_BLACK);
        for (int i = 0; i < VIEW_W; i++) sb.append("　");
        sb.append(RESET).append("\n");

        // Row 50 - empty
        sb.append(RESET + BG_BLACK);
        for (int i = 0; i < VIEW_W; i++) sb.append("　");
        sb.append(RESET).append("\n");
    }

    // ==================== PICKING LOCK OVERLAY ====================
    private void renderPickingLockOverlay(StringBuilder sb) {
        // We need to go back and modify the top lines - but we already rendered
        // Instead, use cursor positioning
        sb.append("\033[1;1H"); // row 1, col 1
        sb.append(RESET + BG_BLACK + BOLD + BRIGHT_YELLOW);
        sb.append("スペースキーを連打してピッキングせよ！");
        sb.append(RESET);

        sb.append("\033[2;1H");
        sb.append(RESET + BG_BLACK + WHITE);
        int prog = model.pickingProgress;
        int total = 30;
        int filled = (prog * 20) / total;
        sb.append("進捗：[");
        for (int i = 0; i < 20; i++) {
            sb.append(i < filled ? "■" : "□");
        }
        sb.append("]　" + prog + "/30");
        sb.append(RESET);
    }

    // ==================== MINIGAME OVERLAY ====================
    private void renderMinigameOverlay(StringBuilder sb) {
        if (model.currentMinigame == null) return;
        Minigame mg = model.currentMinigame;

        // Draw a box in the center of screen (rows 15-35, starting at screen position)
        int boxRow = 15;
        int boxWidth = 50; // full-width chars
        String border = BG_MAGENTA + WHITE;

        // Use cursor positioning to draw overlay
        for (int r = boxRow; r <= boxRow + 18; r++) {
            sb.append("\033[" + r + ";1H");
            sb.append(border);
            for (int c = 0; c < boxWidth; c++) sb.append("　");
            sb.append(RESET);
        }

        switch (mg.type) {
            case Minigame.TYPE_MATH: renderMathMinigame(sb, mg, boxRow); break;
            case Minigame.TYPE_ZC_ALT: renderZCMinigame(sb, mg, boxRow); break;
            case Minigame.TYPE_TIMING: renderTimingMinigame(sb, mg, boxRow); break;
            case Minigame.TYPE_TYPING: renderTypingMinigame(sb, mg, boxRow); break;
        }
    }

    private void renderMathMinigame(StringBuilder sb, Minigame mg, int boxRow) {
        atPos(sb, boxRow + 1, 3);
        sb.append(BOLD + BRIGHT_YELLOW + "【計算ミニゲーム】" + RESET);

        atPos(sb, boxRow + 3, 3);
        sb.append(WHITE + "問題　" + (mg.mathQuestion + 1) + "/10" + RESET);

        atPos(sb, boxRow + 5, 3);
        String opStr = mg.getMathOpStr();
        sb.append(BRIGHT_CYAN + mg.mathA + "　" + opStr + "　" + mg.mathB + "　＝　？" + RESET);

        atPos(sb, boxRow + 7, 3);
        sb.append(WHITE + "回答：　" + mg.mathInput + "　" + RESET);

        if (!mg.mathFeedback.isEmpty()) {
            atPos(sb, boxRow + 9, 3);
            String color = mg.mathFeedback.contains("せいかい") ? BRIGHT_GREEN : BRIGHT_RED;
            sb.append(color + mg.mathFeedback + RESET);
        }

        atPos(sb, boxRow + 12, 3);
        sb.append(CYAN + "数字キーで入力、Ｅｎｔｅｒで確定、ＥＳＣでキャンセル" + RESET);
    }

    private void renderZCMinigame(StringBuilder sb, Minigame mg, int boxRow) {
        atPos(sb, boxRow + 1, 3);
        sb.append(BOLD + BRIGHT_YELLOW + "【ＺＣ交互ミニゲーム】" + RESET);

        atPos(sb, boxRow + 3, 3);
        sb.append(WHITE + "ＺとＣを交互に連打せよ！" + RESET);

        atPos(sb, boxRow + 5, 3);
        String expected = mg.zcExpectZ ? "Ｚ" : "Ｃ";
        sb.append(BRIGHT_CYAN + "次のキー：　" + expected + RESET);

        atPos(sb, boxRow + 7, 3);
        int filled = (mg.zcProgress * 20) / 20;
        sb.append(WHITE + "進捗：[");
        for (int i = 0; i < 20; i++) {
            sb.append(i < mg.zcProgress ? "■" : "□");
        }
        sb.append("]　" + mg.zcProgress + "/20" + RESET);

        atPos(sb, boxRow + 12, 3);
        sb.append(CYAN + "ＥＳＣでキャンセル" + RESET);
    }

    private void renderTimingMinigame(StringBuilder sb, Minigame mg, int boxRow) {
        atPos(sb, boxRow + 1, 3);
        sb.append(BOLD + BRIGHT_YELLOW + "【タイミングミニゲーム】" + RESET);

        atPos(sb, boxRow + 3, 3);
        sb.append(WHITE + "ラウンド　" + (mg.timingRound + 1) + "/5" + RESET);

        atPos(sb, boxRow + 5, 3);
        sb.append(WHITE + "緑ゾーンでスペースを押せ！" + RESET);

        atPos(sb, boxRow + 7, 3);
        // Bar: 30 chars, safe zone = green, cursor = bright
        sb.append(WHITE + "[");
        for (int i = 0; i < 30; i++) {
            boolean inSafe = (i >= mg.timingSafeZone && i < mg.timingSafeZone + 5);
            boolean isCursor = (i == mg.timingCursor);
            if (isCursor) {
                sb.append(BRIGHT_YELLOW + "▼" + RESET + WHITE);
            } else if (inSafe) {
                sb.append(BRIGHT_GREEN + "＝" + RESET + WHITE);
            } else {
                sb.append("－");
            }
        }
        sb.append("]" + RESET);

        atPos(sb, boxRow + 12, 3);
        sb.append(CYAN + "スペースで押す、ＥＳＣでキャンセル" + RESET);
    }

    private void renderTypingMinigame(StringBuilder sb, Minigame mg, int boxRow) {
        atPos(sb, boxRow + 1, 3);
        sb.append(BOLD + BRIGHT_YELLOW + "【タイピングミニゲーム】" + RESET);

        atPos(sb, boxRow + 3, 3);
        sb.append(WHITE + "単語　" + (mg.typingWord + 1) + "/5" + RESET);

        atPos(sb, boxRow + 5, 3);
        String word = Minigame.TYPING_WORDS[mg.typingWord];
        sb.append(BRIGHT_CYAN + "入力する単語：　" + word + RESET);

        atPos(sb, boxRow + 7, 3);
        sb.append(WHITE + "入力：　" + mg.typingInput + "　" + RESET);

        if (!mg.typingFeedback.isEmpty()) {
            atPos(sb, boxRow + 9, 3);
            String color = mg.typingFeedback.contains("せいかい") ? BRIGHT_GREEN : BRIGHT_RED;
            sb.append(color + mg.typingFeedback + RESET);
        }

        atPos(sb, boxRow + 12, 3);
        sb.append(CYAN + "アルファベットで入力、Ｅｎｔｅｒで確定、ＥＳＣでキャンセル" + RESET);
    }

    // ==================== GAME OVER ====================
    private void renderGameOver(StringBuilder sb) {
        clearScreen(sb);
        centerLineRaw(sb, BRIGHT_RED + BOLD + "Ｇ　Ａ　Ｍ　Ｅ　　Ｏ　Ｖ　Ｅ　Ｒ" + RESET, 10);
        centerLineRaw(sb, RED + "■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■" + RESET, 12);
        centerLineRaw(sb, RED + "警備員に発見されました！" + RESET, 16);

        String[] menu = {"リトライ", "タイトルに戻る"};
        for (int i = 0; i < menu.length; i++) {
            String prefix = (i == model.gameOverSelection) ? REVERSE + BRIGHT_YELLOW : WHITE;
            centerLineRaw(sb, prefix + "　" + menu[i] + "　" + RESET, 22 + i * 3);
        }
        centerLineRaw(sb, CYAN + "ＷＳキーで選択、Ｅｎｔｅｒで決定" + RESET, 32);
        fillToRow(sb, 50);
    }

    // ==================== GAME CLEAR ====================
    private void renderGameClear(StringBuilder sb) {
        clearScreen(sb);
        centerLineRaw(sb, BRIGHT_GREEN + BOLD + "Ｇ　Ａ　Ｍ　Ｅ　　Ｃ　Ｌ　Ｅ　Ａ　Ｒ　！" + RESET, 8);
        centerLineRaw(sb, GREEN + "脱出成功！おめでとう！" + RESET, 11);

        int mm = model.elapsedSeconds / 60;
        int ss = model.elapsedSeconds % 60;
        centerLineRaw(sb, BRIGHT_YELLOW + String.format("クリア時間：%02d分%02d秒", mm, ss) + RESET, 14);

        if (!model.nameEntered) {
            centerLineRaw(sb, WHITE + "お名前を入力してください（英数字10文字以内）" + RESET, 18);
            centerLineRaw(sb, BRIGHT_CYAN + ">" + model.playerName + "_" + RESET, 20);
            centerLineRaw(sb, CYAN + "Ｅｎｔｅｒで確定" + RESET, 22);
        } else {
            if (model.ranking.qualifies(model.elapsedSeconds)) {
                centerLineRaw(sb, BRIGHT_YELLOW + "ランキングに登録されました！" + RESET, 18);
            }
            String[] menu = {"リトライ", "タイトルに戻る"};
            for (int i = 0; i < menu.length; i++) {
                String prefix = (i == model.gameClearSelection) ? REVERSE + BRIGHT_YELLOW : WHITE;
                centerLineRaw(sb, prefix + "　" + menu[i] + "　" + RESET, 24 + i * 3);
            }
            centerLineRaw(sb, CYAN + "ＷＳキーで選択、Ｅｎｔｅｒで決定" + RESET, 34);
        }
        fillToRow(sb, 50);
    }

    // ==================== UTILITIES ====================
    private int currentRow = 1;

    private void clearScreen(StringBuilder sb) {
        for (int i = 0; i < SCREEN_ROWS; i++) {
            sb.append(BG_BLACK);
            for (int c = 0; c < VIEW_W; c++) sb.append("　");
            sb.append(RESET).append("\n");
        }
        currentRow = SCREEN_ROWS + 1;
    }

    private void centerLine(StringBuilder sb, String text, int row) {
        // text contains ANSI codes and full-width chars; approximate centering
        atPos(sb, row, 1);
        sb.append(BG_BLACK);
        for (int c = 0; c < VIEW_W; c++) sb.append("　");
        // Now place text at approximate center
        atPos(sb, row, VIEW_W / 2 - 10);
        sb.append(text);
        sb.append(RESET);
    }

    private void centerLineRaw(StringBuilder sb, String text, int row) {
        atPos(sb, row, 1);
        sb.append(BG_BLACK);
        for (int c = 0; c < VIEW_W; c++) sb.append("　");
        atPos(sb, row, VIEW_W / 2 - 15);
        sb.append(text);
        sb.append(RESET);
    }

    private void printLine(StringBuilder sb, String text, int row) {
        atPos(sb, row, 1);
        sb.append(BG_BLACK);
        for (int c = 0; c < VIEW_W; c++) sb.append("　");
        atPos(sb, row, 1);
        sb.append(text);
        sb.append(RESET);
    }

    private void atPos(StringBuilder sb, int row, int col) {
        // col here is in full-width chars, so terminal col = col * 2 - 1
        int termCol = (col - 1) * 2 + 1;
        sb.append("\033[" + row + ";" + termCol + "H");
    }

    private void fillToRow(StringBuilder sb, int targetRow) {
        // This is called after clearScreen which already fills all rows,
        // so nothing needed
    }
}
