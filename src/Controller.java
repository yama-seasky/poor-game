public class Controller {

    public void handleInput(String key, GameState state) {
        switch (state.phase) {
            case START_MENU:    handleMenu(key, state); break;
            case HOW_TO_PLAY:   state.phase = GameState.GamePhase.START_MENU; break;
            case RANKING:       state.phase = GameState.GamePhase.START_MENU; break;
            case PLAYING:       handlePlaying(key, state); break;
            case PICKING_LOCK:  handlePickingLock(key, state); break;
            case GIMMICK:       handleGimmick(key, state); break;
            case GAME_OVER:     handleGameOver(key, state); break;
            case GAME_CLEAR:    handleGameClear(key, state); break;
            case NAME_INPUT:    handleNameInput(key, state); break;
            default: break;
        }
    }

    private void handleMenu(String key, GameState state) {
        int n = GameState.MENU_ITEMS.length;
        if (key.equals("UP") || key.equals("w")) {
            state.menuIndex = (state.menuIndex - 1 + n) % n;
        } else if (key.equals("DOWN") || key.equals("s")) {
            state.menuIndex = (state.menuIndex + 1) % n;
        } else if (key.equals(" ") || key.equals("") || key.equals("ENTER")) {
            switch (state.menuIndex) {
                case 0: state.initGame(); break;
                case 1: state.phase = GameState.GamePhase.RANKING; break;
                case 2: state.phase = GameState.GamePhase.HOW_TO_PLAY; break;
                case 3: state.phase = GameState.GamePhase.EXITING; break;
            }
        }
    }

    private void handlePlaying(String key, GameState state) {
        if (key.equals("q") || key.equals("Q")) {
            state.phase = GameState.GamePhase.START_MENU;
            return;
        }
        // Movement
        int nx = state.player.x;
        int ny = state.player.y;
        String dir = null;
        if (key.equals("UP") || key.equals("w")) { ny--; dir = "UP"; }
        else if (key.equals("DOWN") || key.equals("s")) { ny++; dir = "DOWN"; }
        else if (key.equals("LEFT") || key.equals("a")) { nx--; dir = "LEFT"; }
        else if (key.equals("RIGHT") || key.equals("d")) { nx++; dir = "RIGHT"; }

        if (dir != null) {
            state.player.direction = dir;
            if (canMove(nx, ny, dir, state.map)) {
                state.player.x = nx;
                state.player.y = ny;
                // Check exit
                char tile = state.map.getTile(state.player.x, state.player.y + 1);
                if (tile == MapData.EXIT || state.map.getTile(nx, ny) == MapData.EXIT
                    || state.map.getTile(nx, ny+2) == MapData.EXIT) {
                    state.phase = GameState.GamePhase.GAME_CLEAR;
                    if (state.ranking.isTopFive(state.gameTimeMs)) {
                        state.phase = GameState.GamePhase.NAME_INPUT;
                    }
                }
            }
        } else if (key.equals(" ")) {
            handleSpace(state);
        }
    }

    private boolean canMove(int nx, int ny, String dir, MapData map) {
        // Sprite occupies (nx,ny), (nx,ny+1), (nx,ny+2)
        switch (dir) {
            case "UP":    return map.isPassable(nx, ny);
            case "DOWN":  return map.isPassable(nx, ny + 2);
            case "LEFT":
            case "RIGHT":
                return map.isPassable(nx, ny) && map.isPassable(nx, ny + 1) && map.isPassable(nx, ny + 2);
        }
        return false;
    }

    private void handleSpace(GameState state) {
        int px = state.player.x;
        int py = state.player.y;
        // Check adjacent LOCKED_DOOR
        for (int dy = -1; dy <= 3; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (state.map.getTile(px + dx, py + dy) == MapData.LOCKED_DOOR) {
                    state.phase = GameState.GamePhase.PICKING_LOCK;
                    return;
                }
            }
        }
        // Check adjacent SECURITY_PANEL_ACTIVE
        for (int i = 0; i < state.panels.length; i++) {
            SecurityPanel p = state.panels[i];
            if (p.isDone()) continue;
            int pdx = Math.abs(p.col - px);
            int pdy = p.row - (py + 1);
            if (pdx <= 2 && pdy >= -2 && pdy <= 3) {
                state.activePanel = i;
                if (p.currentMiniGame == null) {
                    p.assignRandomMiniGame();
                }
                state.phase = GameState.GamePhase.GIMMICK;
                return;
            }
        }
    }

    private void handlePickingLock(String key, GameState state) {
        if (key.equals(" ")) {
            state.pickingProgress = Math.min(100f, state.pickingProgress + 8f);
            if (state.pickingProgress >= 100f) {
                // Open the door
                state.map.setTile(13, 7, MapData.OPEN_DOOR);
                state.pickingProgress = 0f;
                state.phase = GameState.GamePhase.PLAYING;
            }
        } else if (!key.isEmpty()) {
            // any other key returns to playing (progress saved)
            state.phase = GameState.GamePhase.PLAYING;
        }
    }

    private void handleGimmick(String key, GameState state) {
        if (key.equals("ESCAPE") || key.equals("\033") || key.equalsIgnoreCase("q")) {
            state.phase = GameState.GamePhase.PLAYING;
            state.activePanel = -1;
            return;
        }
        if (state.activePanel >= 0 && state.panels[state.activePanel].currentMiniGame != null) {
            MiniGame mg = state.panels[state.activePanel].currentMiniGame;
            if (!mg.isDone()) {
                mg.handleKey(key);
                // Check if done after key
                if (mg.isDone()) {
                    SecurityPanel p = state.panels[state.activePanel];
                    p.progress = 100f;
                    state.map.setTile(p.col, p.row, MapData.SECURITY_PANEL_DONE);
                    state.checkGateOpen();
                }
            } else {
                // Already done - any key returns
                state.phase = GameState.GamePhase.PLAYING;
                state.activePanel = -1;
            }
        }
    }

    private void handleGameOver(String key, GameState state) {
        int n = GameState.GAMEOVER_MENU.length;
        if (key.equals("UP") || key.equals("w")) {
            state.gameOverMenuIndex = (state.gameOverMenuIndex - 1 + n) % n;
        } else if (key.equals("DOWN") || key.equals("s")) {
            state.gameOverMenuIndex = (state.gameOverMenuIndex + 1) % n;
        } else if (key.equals(" ") || key.equals("") || key.equals("ENTER")) {
            if (state.gameOverMenuIndex == 0) {
                state.initGame();
            } else {
                state.phase = GameState.GamePhase.START_MENU;
                state.menuIndex = 0;
            }
        }
    }

    private void handleGameClear(String key, GameState state) {
        int n = GameState.GAMECLEAR_MENU.length;
        if (key.equals("UP") || key.equals("w")) {
            state.gameClearMenuIndex = (state.gameClearMenuIndex - 1 + n) % n;
        } else if (key.equals("DOWN") || key.equals("s")) {
            state.gameClearMenuIndex = (state.gameClearMenuIndex + 1) % n;
        } else if (key.equals(" ") || key.equals("") || key.equals("ENTER")) {
            if (state.gameClearMenuIndex == 0) {
                state.initGame();
            } else {
                state.phase = GameState.GamePhase.START_MENU;
                state.menuIndex = 0;
            }
        }
    }

    private void handleNameInput(String key, GameState state) {
        if (key.equals("") || key.equals("ENTER")) {
            String name = state.inputName.isEmpty() ? "PLAYER" : state.inputName;
            state.ranking.addEntry(name, state.gameTimeMs);
            state.nameSubmitted = true;
            state.phase = GameState.GamePhase.GAME_CLEAR;
        } else if (key.equals("BACKSPACE") || key.equals("\b")) {
            if (!state.inputName.isEmpty()) {
                state.inputName = state.inputName.substring(0, state.inputName.length() - 1);
            }
        } else if (key.length() == 1) {
            char c = key.charAt(0);
            if (c >= 32 && c < 127 && state.inputName.length() < 8) {
                state.inputName += c;
            }
        }
    }
}
