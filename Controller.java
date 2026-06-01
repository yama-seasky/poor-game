public class Controller {
    private Model model;

    public Controller(Model model) {
        this.model = model;
    }

    public void processInput(String key) {
        switch (model.state) {
            case Model.STATE_TITLE: processTitle(key); break;
            case Model.STATE_HOW_TO_PLAY: processHowToPlay(key); break;
            case Model.STATE_RANKING: processRanking(key); break;
            case Model.STATE_PICKING_LOCK: processPickingLock(key); break;
            case Model.STATE_PLAYING: processPlaying(key); break;
            case Model.STATE_MINIGAME: processMinigame(key); break;
            case Model.STATE_GAME_OVER: processGameOver(key); break;
            case Model.STATE_GAME_CLEAR: processGameClear(key); break;
        }
    }

    private void processTitle(String key) {
        if (key.equals("w") || key.equals("W") || key.equals("UP")) {
            model.titleSelection = (model.titleSelection - 1 + 4) % 4;
        } else if (key.equals("s") || key.equals("S") || key.equals("DOWN")) {
            model.titleSelection = (model.titleSelection + 1) % 4;
        } else if (key.equals(" ") || key.isEmpty()) {
            // ENTER or SPACE confirm
            switch (model.titleSelection) {
                case 0: model.initGame(); break;
                case 1: model.state = Model.STATE_RANKING; break;
                case 2: model.state = Model.STATE_HOW_TO_PLAY; break;
                case 3: System.exit(0); break;
            }
        }
    }

    private void processHowToPlay(String key) {
        if (key.equals("ESC") || key.isEmpty()) {
            model.state = Model.STATE_TITLE;
        }
    }

    private void processRanking(String key) {
        if (key.equals("ESC") || key.isEmpty()) {
            model.state = Model.STATE_TITLE;
        }
    }

    private void processPickingLock(String key) {
        if (key.equals(" ")) {
            model.pickingProgress++;
            if (model.pickingProgress >= 30) {
                model.map.tiles[44][23] = GameMap.DOOR_OPEN;
                model.state = Model.STATE_PLAYING;
            }
        } else {
            // Allow movement
            movePlayer(key);
        }
    }

    private void processPlaying(String key) {
        if (movePlayer(key)) return;
        if (key.equals("e") || key.equals("E")) {
            int idx = model.getNearbyDeviceIndex();
            if (idx >= 0) {
                model.activeDeviceIndex = idx;
                SecurityDevice d = model.devices[idx];
                if (model.currentMinigame == null || model.currentMinigame.completed) {
                    model.currentMinigame = new Minigame(d.type);
                } else {
                    // resume existing minigame for this device - create new if needed
                    model.currentMinigame = new Minigame(d.type);
                }
                model.state = Model.STATE_MINIGAME;
            }
        }
    }

    private boolean movePlayer(String key) {
        int nx = model.player.x;
        int ny = model.player.y;
        if (key.equals("w") || key.equals("W") || key.equals("UP")) {
            ny--;
            model.player.direction = Player.DIR_UP;
        } else if (key.equals("s") || key.equals("S") || key.equals("DOWN")) {
            ny++;
            model.player.direction = Player.DIR_DOWN;
        } else if (key.equals("a") || key.equals("A") || key.equals("LEFT")) {
            nx--;
            model.player.direction = Player.DIR_LEFT;
        } else if (key.equals("d") || key.equals("D") || key.equals("RIGHT")) {
            nx++;
            model.player.direction = Player.DIR_RIGHT;
        } else {
            return false;
        }
        if (model.map.isWalkable(nx, ny)) {
            model.player.x = nx;
            model.player.y = ny;
        }
        return true;
    }

    private void processMinigame(String key) {
        if (model.currentMinigame == null) {
            model.state = Model.STATE_PLAYING;
            return;
        }
        boolean done = model.currentMinigame.processInput(key);
        if (done) {
            if (model.currentMinigame.completed) {
                model.clearDevice(model.activeDeviceIndex);
            }
            // ESC or completed - return to playing
            model.state = Model.STATE_PLAYING;
            model.currentMinigame = null;
        }
    }

    private void processGameOver(String key) {
        if (key.equals("w") || key.equals("W") || key.equals("UP")) {
            model.gameOverSelection = (model.gameOverSelection - 1 + 2) % 2;
        } else if (key.equals("s") || key.equals("S") || key.equals("DOWN")) {
            model.gameOverSelection = (model.gameOverSelection + 1) % 2;
        } else if (key.equals(" ") || key.isEmpty()) {
            if (model.gameOverSelection == 0) {
                model.initGame();
            } else {
                model.state = Model.STATE_TITLE;
            }
        }
    }

    private void processGameClear(String key) {
        if (!model.nameEntered) {
            // Name input
            if (key.isEmpty()) { // ENTER
                if (model.playerName.isEmpty()) model.playerName = "PLAYER";
                model.nameEntered = true;
                if (model.ranking.qualifies(model.elapsedSeconds)) {
                    model.ranking.addEntry(model.playerName, model.elapsedSeconds);
                }
            } else if (key.equals("ESC")) {
                model.playerName = "PLAYER";
                model.nameEntered = true;
                if (model.ranking.qualifies(model.elapsedSeconds)) {
                    model.ranking.addEntry(model.playerName, model.elapsedSeconds);
                }
            } else if (key.length() == 1) {
                char c = key.charAt(0);
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                    if (model.playerName.length() < 10) {
                        model.playerName += c;
                    }
                }
            }
        } else {
            // After name entry, select retry or title
            if (key.equals("w") || key.equals("W") || key.equals("UP")) {
                model.gameClearSelection = (model.gameClearSelection - 1 + 2) % 2;
            } else if (key.equals("s") || key.equals("S") || key.equals("DOWN")) {
                model.gameClearSelection = (model.gameClearSelection + 1) % 2;
            } else if (key.equals(" ") || key.isEmpty()) {
                if (model.gameClearSelection == 0) {
                    model.initGame();
                } else {
                    model.state = Model.STATE_TITLE;
                }
            }
        }
    }
}
