import java.io.PrintStream;

public class View {
    // Viewport: 100 full-width chars wide, 50 rows tall
    private static final int VIEW_W = 100;
    private static final int VIEW_H = 50;

    public void render(GameState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[H"); // cursor home

        switch (state.phase) {
            case START_MENU:   renderStartMenu(sb, state); break;
            case HOW_TO_PLAY:  renderHowToPlay(sb); break;
            case RANKING:      renderRanking(sb, state); break;
            case PLAYING:      renderGame(sb, state); break;
            case PICKING_LOCK: renderGame(sb, state); renderPickingLock(sb, state); break;
            case GIMMICK:      renderGame(sb, state); renderGimmick(sb, state); break;
            case GAME_OVER:    renderGame(sb, state); renderGameOver(sb, state); break;
            case GAME_CLEAR:   renderGame(sb, state); renderGameClear(sb, state); break;
            case NAME_INPUT:   renderGame(sb, state); renderNameInput(sb, state); break;
            default: break;
        }

        System.out.print(sb.toString());
    }

    private void renderStartMenu(StringBuilder sb, GameState state) {
        clearScreen(sb);
        sb.append("\033[8;1H");
        centerLine(sb, "■■■■■■■■■■■■■■■■");
        sb.append("\033[9;1H");
        centerLine(sb, "■　Ｆｏｒｔｒｅｓｓ　ｏｆ　Ｄｅｓｐａｉｒ　■");
        sb.append("\033[10;1H");
        centerLine(sb, "■■■■■■■■■■■■■■■■");

        for (int i = 0; i < GameState.MENU_ITEMS.length; i++) {
            sb.append(String.format("\033[%d;1H", 13 + i * 2));
            String item = GameState.MENU_ITEMS[i];
            String line = "　　　　　　　　　　　　" + item + "　　　　　　　　　　　　";
            if (i == state.menuIndex) {
                sb.append("\033[7m").append(line).append("\033[0m");
            } else {
                sb.append(line);
            }
        }

        sb.append("\033[22;1H");
        centerLine(sb, "ＷＳ/上下で選択、スペース/Ｅｎｔｅｒで決定");
    }

    private void centerLine(StringBuilder sb, String text) {
        // text is full-width, each char = 2 terminal cols. VIEW_W full-width chars = 200 terminal cols.
        // terminal width ~200 (100 full-width)
        int textWidth = text.length(); // in full-width chars
        int pad = (VIEW_W - textWidth) / 2;
        for (int i = 0; i < pad; i++) sb.append('　');
        sb.append(text);
    }

    private void renderHowToPlay(StringBuilder sb) {
        clearScreen(sb);
        sb.append("\033[3;1H");
        centerLine(sb, "■■■■　遊　び　方　■■■■");
        sb.append("\033[5;1H　　　方向キーまたはＷＡＳＤで移動");
        sb.append("\033[6;1H　　　スペースキーでドア解錠・パネル操作");
        sb.append("\033[7;1H　　　セキュリティパネルを４つ以上無力化してゲートを開け");
        sb.append("\033[8;1H　　　出口（出）まで到達すればクリア！");
        sb.append("\033[9;1H　　　看守（看）に見つかるとゲームオーバー");
        sb.append("\033[10;1H　　　ＱキーでタイトルへESCで中断");
        sb.append("\033[13;1H");
        centerLine(sb, "何かキーを押すとタイトルへ戻る");
    }

    private void renderRanking(StringBuilder sb, GameState state) {
        clearScreen(sb);
        sb.append("\033[3;1H");
        centerLine(sb, "■■■■　ラ　ン　キ　ン　グ　■■■■");
        if (state.ranking.entries.isEmpty()) {
            sb.append("\033[6;1H");
            centerLine(sb, "　まだ記録がありません　");
        } else {
            for (int i = 0; i < state.ranking.entries.size(); i++) {
                Ranking.Entry e = state.ranking.entries.get(i);
                sb.append(String.format("\033[%d;1H　　　%d位　%s　　%s",
                    6 + i * 2, i + 1, e.name, state.ranking.formatTime(e.timeMs)));
            }
        }
        sb.append("\033[18;1H");
        centerLine(sb, "何かキーを押すとタイトルへ戻る");
    }

    private void renderGame(StringBuilder sb, GameState state) {
        // Compute viewport: center on player
        int px = state.player.x;
        int py = state.player.y;

        // viewport top-left in map coords
        int vpx = px - VIEW_W / 2;
        int vpy = py + 1 - VIEW_H / 2; // center on player's middle tile

        // Clamp
        if (vpx < 0) vpx = 0;
        if (vpy < 0) vpy = 0;
        if (vpx + VIEW_W > MapData.MAP_W) vpx = MapData.MAP_W - VIEW_W;
        if (vpy + VIEW_H > MapData.MAP_H) vpy = MapData.MAP_H - VIEW_H;

        // Build char grid for viewport
        char[][] grid = new char[VIEW_H][VIEW_W];
        String[][] colors = new String[VIEW_H][VIEW_W];

        for (int r = 0; r < VIEW_H; r++) {
            for (int c = 0; c < VIEW_W; c++) {
                int mc = vpx + c;
                int mr = vpy + r;
                char tile = state.map.getTile(mc, mr);
                grid[r][c] = tile;
                colors[r][c] = tileColor(tile);
            }
        }

        // Draw guards
        for (Guard g : state.guards) {
            int[] gscr = mapToScreen(g.x, g.y, vpx, vpy);
            if (gscr != null) {
                setCell(grid, colors, gscr[0], gscr[1], '看', "\033[93m");
            }
            if (gscr != null) {
                setCell(grid, colors, gscr[0], gscr[1] + 1, g.getDirectionChar(), "\033[93m");
                setCell(grid, colors, gscr[0], gscr[1] + 2, 'Ａ', "\033[93m");
            }
        }

        // Draw player
        int[] pscr = mapToScreen(state.player.x, state.player.y, vpx, vpy);
        if (pscr != null) {
            setCell(grid, colors, pscr[0], pscr[1], '＠', "\033[92m");
            setCell(grid, colors, pscr[0], pscr[1] + 1, state.player.getDirectionChar(), "\033[92m");
            setCell(grid, colors, pscr[0], pscr[1] + 2, 'Ａ', "\033[92m");
        }

        // Render grid
        for (int r = 0; r < VIEW_H; r++) {
            sb.append(String.format("\033[%d;1H", r + 1));
            String prevColor = "";
            for (int c = 0; c < VIEW_W; c++) {
                String col = colors[r][c] != null ? colors[r][c] : "";
                if (!col.equals(prevColor)) {
                    if (!prevColor.isEmpty()) sb.append("\033[0m");
                    if (!col.isEmpty()) sb.append(col);
                    prevColor = col;
                }
                sb.append(grid[r][c]);
            }
            if (!prevColor.isEmpty()) sb.append("\033[0m");
        }

        // HUD rows 51-52
        sb.append("\033[51;1H\033[44m\033[0;44m");
        String hudLine1 = String.format("　時間：%s　　パネル：%d/６　完了済み：%d　ゲート：%s　",
            state.ranking.formatTime(state.gameTimeMs),
            state.countPanelsDone(),
            state.countPanelsDone(),
            state.map.getTile(MapData.GATE_COL, MapData.GATE_ROW) == MapData.GATE_OPEN ? "開" : "閉"
        );
        // Pad to 100 full-width chars
        sb.append(padFW(hudLine1, 100));
        sb.append("\033[0m");
        sb.append("\033[52;1H\033[44m");
        String hudLine2 = "　移動：ＷＡＳＤ/矢印　スペース：操作　Ｑ：タイトル　";
        sb.append(padFW(hudLine2, 100));
        sb.append("\033[0m");
    }

    private String tileColor(char tile) {
        switch (tile) {
            case MapData.SECURITY_PANEL_ACTIVE: return "\033[96m";
            case MapData.SECURITY_PANEL_DONE:   return "\033[90m";
            case MapData.EXIT:                  return "\033[93m";
            default: return "";
        }
    }

    private void setCell(char[][] grid, String[][] colors, int c, int r, char ch, String color) {
        if (r >= 0 && r < grid.length && c >= 0 && c < grid[0].length) {
            grid[r][c] = ch;
            colors[r][c] = color;
        }
    }

    private int[] mapToScreen(int mx, int my, int vpx, int vpy) {
        int sc = mx - vpx;
        int sr = my - vpy;
        if (sc < 0 || sc >= VIEW_W) return null;
        // rows may be partially off
        return new int[]{sc, sr};
    }

    private void renderPickingLock(StringBuilder sb, GameState state) {
        sb.append("\033[10;20H\033[96m┌──────────────────────────────────────────┐");
        sb.append("\033[11;20H│\033[0m\033[96m　　　錠前解錠ミニゲーム　　　　　　　　　　　│");
        sb.append("\033[12;20H│\033[0m　　スペースキーを連打してピッキングせよ！　　\033[96m│");

        int filled = (int)(state.pickingProgress / 10f);
        StringBuilder gauge = new StringBuilder("　[");
        for (int i = 0; i < 10; i++) gauge.append(i < filled ? "■" : "□");
        gauge.append(String.format("]　%4.0f%%", state.pickingProgress));
        sb.append(String.format("\033[13;20H│\033[0m%s　　　　　　　　\033[96m│", gauge.toString()));

        sb.append("\033[14;20H│\033[0m　スペースで連打、他キーで中断（進捗保存）　　\033[96m│");
        sb.append("\033[15;20H└──────────────────────────────────────────┘\033[0m");
    }

    private void renderGimmick(StringBuilder sb, GameState state) {
        if (state.activePanel >= 0 && state.panels[state.activePanel].currentMiniGame != null) {
            state.panels[state.activePanel].currentMiniGame.renderOverlay(sb);
        }
    }

    private void renderGameOver(StringBuilder sb, GameState state) {
        sb.append("\033[18;1H");
        centerLine(sb, "Ｇ　Ａ　Ｍ　Ｅ　　Ｏ　Ｖ　Ｅ　Ｒ");
        for (int i = 0; i < GameState.GAMEOVER_MENU.length; i++) {
            sb.append(String.format("\033[%d;1H", 21 + i * 2));
            String item = "　　　　　　　　　　　　" + GameState.GAMEOVER_MENU[i] + "　　　　　　　　　　　　";
            if (i == state.gameOverMenuIndex) {
                sb.append("\033[7m").append(item).append("\033[0m");
            } else {
                sb.append(item);
            }
        }
    }

    private void renderGameClear(StringBuilder sb, GameState state) {
        sb.append("\033[17;1H");
        centerLine(sb, "Ｇ　Ａ　Ｍ　Ｅ　　Ｃ　Ｌ　Ｅ　Ａ　Ｒ　！");
        sb.append("\033[19;1H");
        centerLine(sb, "クリアタイム：" + state.ranking.formatTime(state.gameTimeMs));
        for (int i = 0; i < GameState.GAMECLEAR_MENU.length; i++) {
            sb.append(String.format("\033[%d;1H", 22 + i * 2));
            String item = "　　　　　　　　　　　　" + GameState.GAMECLEAR_MENU[i] + "　　　　　　　　　　　　";
            if (i == state.gameClearMenuIndex) {
                sb.append("\033[7m").append(item).append("\033[0m");
            } else {
                sb.append(item);
            }
        }
    }

    private void renderNameInput(StringBuilder sb, GameState state) {
        sb.append("\033[17;1H");
        centerLine(sb, "Ｇ　Ａ　Ｍ　Ｅ　　Ｃ　Ｌ　Ｅ　Ａ　Ｒ　！");
        sb.append("\033[19;1H");
        centerLine(sb, "クリアタイム：" + state.ranking.formatTime(state.gameTimeMs));
        sb.append("\033[21;1H");
        centerLine(sb, "名前を入力してください（最大８文字）");
        sb.append("\033[23;1H");
        centerLine(sb, "名前：" + state.inputName + "＿");
        sb.append("\033[25;1H");
        centerLine(sb, "Enterで確定");
    }

    private void clearScreen(StringBuilder sb) {
        sb.append("\033[2J\033[H");
    }

    private String padFW(String s, int targetLen) {
        int len = s.length();
        StringBuilder sb = new StringBuilder(s);
        while (len < targetLen) { sb.append('　'); len++; }
        return sb.toString();
    }
}
