import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Model {
    // Game states
    public static final int STATE_TITLE = 0;
    public static final int STATE_HOW_TO_PLAY = 1;
    public static final int STATE_RANKING = 2;
    public static final int STATE_PICKING_LOCK = 3;
    public static final int STATE_PLAYING = 4;
    public static final int STATE_MINIGAME = 5;
    public static final int STATE_GAME_OVER = 6;
    public static final int STATE_GAME_CLEAR = 7;

    public int state;
    public int titleSelection; // 0-3
    public int gameOverSelection; // 0=retry, 1=title
    public int gameClearSelection; // 0=retry, 1=title

    public GameMap map;
    public Player player;
    public Guard[] guards;
    public SecurityDevice[] devices;
    public Ranking ranking;

    public int pickingProgress; // 0-29
    public int devicesCleared;
    public int activeDeviceIndex; // which device minigame is active
    public Minigame currentMinigame;

    public long gameStartTime; // System.currentTimeMillis
    public int elapsedSeconds;

    public int gameTick;

    // Name entry for GAME_CLEAR
    public String playerName;
    public boolean nameEntered;

    // Contextual message for HUD
    public String hudMessage;

    // Input queue
    public BlockingQueue<String> inputQueue;

    // Output stream
    public PrintStream out;

    public Model() throws Exception {
        out = new PrintStream(System.out, true, "UTF-8");
        inputQueue = new LinkedBlockingQueue<>();
        ranking = new Ranking();
        state = STATE_TITLE;
        titleSelection = 0;
        gameOverSelection = 0;
        gameClearSelection = 0;
        hudMessage = "";
        playerName = "";
        nameEntered = false;
    }

    public void initGame() {
        map = new GameMap();
        player = new Player(10, 44);
        devices = new SecurityDevice[6];
        // Assign types: 0-3 get type 0-3, 4-5 get types 0,1
        int[] types = {0, 1, 2, 3, 0, 1};
        int[][] devPos = {{38,12},{38,67},{74,12},{74,67},{104,12},{104,67}};
        for (int i = 0; i < 6; i++) {
            devices[i] = new SecurityDevice(devPos[i][0], devPos[i][1], types[i], i);
        }

        guards = new Guard[10];
        guards[0] = new Guard(5, 37, new int[][]{{5,37},{5,53},{20,53},{20,37}});
        guards[1] = new Guard(26, 5, new int[][]{{26,5},{56,5},{56,22},{26,22}});
        guards[2] = new Guard(26, 58, new int[][]{{26,58},{56,58},{56,74},{26,74}});
        guards[3] = new Guard(64, 5, new int[][]{{64,5},{86,5}});
        guards[4] = new Guard(26, 40, new int[][]{{26,40},{126,40}});
        guards[5] = new Guard(90, 5, new int[][]{{90,5},{120,5},{120,22},{90,22}});
        guards[6] = new Guard(90, 58, new int[][]{{90,58},{120,58},{120,74},{90,74}});
        guards[7] = new Guard(130, 5, new int[][]{{130,5},{145,5},{145,74},{130,74}});
        guards[8] = new Guard(130, 30, new int[][]{{130,30},{145,30},{145,50},{130,50}});
        guards[9] = new Guard(133, 40, new int[][]{{133,40},{145,40}});

        pickingProgress = 0;
        devicesCleared = 0;
        activeDeviceIndex = -1;
        currentMinigame = null;
        gameStartTime = System.currentTimeMillis();
        elapsedSeconds = 0;
        gameTick = 0;
        hudMessage = "";
        playerName = "";
        nameEntered = false;
        state = STATE_PICKING_LOCK;
    }

    public void updateGuards() {
        if (gameTick % 8 != 0) return;
        for (Guard g : guards) {
            g.update(map);
        }
    }

    public boolean checkDetection() {
        for (Guard g : guards) {
            if (g.detectsPlayer(player.x, player.y, map)) {
                return true;
            }
        }
        return false;
    }

    public void checkDevicesNearby() {
        hudMessage = "";
        for (int i = 0; i < devices.length; i++) {
            SecurityDevice d = devices[i];
            if (!d.cleared) {
                int dx = Math.abs(player.x - d.x);
                int dy = Math.abs(player.y - d.y);
                if (dx <= 1 && dy <= 1) {
                    hudMessage = "Ｅキーで保安装置を解除";
                    break;
                }
            }
        }
    }

    public int getNearbyDeviceIndex() {
        for (int i = 0; i < devices.length; i++) {
            SecurityDevice d = devices[i];
            if (!d.cleared) {
                int dx = Math.abs(player.x - d.x);
                int dy = Math.abs(player.y - d.y);
                if (dx <= 1 && dy <= 1) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void clearDevice(int index) {
        devices[index].cleared = true;
        devicesCleared++;
        // Update map tile
        map.tiles[devices[index].y][devices[index].x] = GameMap.DEVICE_BASE + 6 + index;
        if (devicesCleared >= 4) {
            map.openGate();
        }
    }

    public static void main(String[] args) throws Exception {
        Model model = new Model();
        View view = new View(model);
        Controller controller = new Controller(model);

        // Hide cursor
        model.out.print("\033[?25l");
        model.out.flush();

        // Shutdown hook to show cursor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print("\033[?25h\033[0m\n");
            System.out.flush();
        }));

        // Input thread
        Thread inputThread = new Thread(() -> {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    model.inputQueue.put(line);
                }
            } catch (Exception e) {
                // stdin closed
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        // Game loop
        long lastTime = System.currentTimeMillis();
        while (true) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            if (elapsed < 50) {
                Thread.sleep(50 - elapsed);
            }
            lastTime = System.currentTimeMillis();

            // Drain input
            List<String> inputs = new ArrayList<>();
            model.inputQueue.drainTo(inputs);

            // Update game time
            if (model.state == STATE_PLAYING || model.state == STATE_MINIGAME || model.state == STATE_PICKING_LOCK) {
                model.elapsedSeconds = (int)((System.currentTimeMillis() - model.gameStartTime) / 1000);
            }

            // Update tick
            if (model.state == STATE_PLAYING || model.state == STATE_MINIGAME || model.state == STATE_PICKING_LOCK) {
                model.gameTick++;

                // Update timing minigame cursor
                if (model.state == STATE_MINIGAME && model.currentMinigame != null) {
                    model.currentMinigame.tickTiming();
                }

                // Update guards
                model.updateGuards();

                // Check detection
                if (model.checkDetection()) {
                    model.state = STATE_GAME_OVER;
                    model.gameOverSelection = 0;
                }
            }

            // Process inputs
            for (String key : inputs) {
                if (model.state == STATE_GAME_OVER && key.equals("ESC")) {
                    model.state = STATE_TITLE;
                } else {
                    controller.processInput(key);
                }
                if (model.state == STATE_GAME_CLEAR && model.nameEntered) {
                    // handled in controller
                }
            }

            // Check nearby devices
            if (model.state == STATE_PLAYING) {
                model.checkDevicesNearby();
                // Check exit
                if (model.map.tiles[model.player.y][model.player.x] == GameMap.EXIT) {
                    model.state = STATE_GAME_CLEAR;
                    model.gameClearSelection = 0;
                }
            }

            // Render
            view.render();
        }
    }
}
