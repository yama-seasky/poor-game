import java.io.*;
import java.util.concurrent.*;

public class Model {
    public static void main(String[] args) throws Exception {
        // Set UTF-8 output
        PrintStream utf8out = new PrintStream(System.out, true, "UTF-8");
        System.setOut(utf8out);

        // Hide cursor
        System.out.print("\033[?25l");

        GameState state = new GameState();
        state.phase = GameState.GamePhase.START_MENU;
        View view = new View();
        Controller controller = new Controller();

        // Input thread
        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
        Thread inputThread = new Thread(() -> {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    inputQueue.offer(line);
                }
            } catch (Exception e) {
                // stdin closed
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        long lastTime = System.currentTimeMillis();
        final long FRAME_MS = 100;

        // Main loop ~10fps
        while (state.phase != GameState.GamePhase.EXITING) {
            long now = System.currentTimeMillis();
            long dt = now - lastTime;
            lastTime = now;

            // Process all pending inputs
            String key;
            while ((key = inputQueue.poll()) != null) {
                controller.handleInput(key, state);
                if (state.phase == GameState.GamePhase.EXITING) break;
            }

            // Update state
            if (state.phase != GameState.GamePhase.EXITING) {
                state.update(dt);
                view.render(state);
            }

            // Sleep remaining frame time
            long elapsed = System.currentTimeMillis() - now;
            long sleep = FRAME_MS - elapsed;
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
        }

        // Cleanup
        System.out.print("\033[?25h");
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }
}
