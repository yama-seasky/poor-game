public class TimingMiniGame extends MiniGame {
    private float barProgress = 0f; // 0-100
    private float barSpeed = 30f;   // % per second
    private boolean filling = true;
    private String message = "";
    private int rounds = 0;
    private static final int MAX_ROUNDS = 5;

    public TimingMiniGame(SecurityPanel panel) {
        super(panel);
    }

    @Override
    public void update(long dt) {
        if (isDone()) return;
        float delta = barSpeed * dt / 1000f;
        if (filling) {
            barProgress += delta;
            if (barProgress >= 100f) {
                barProgress = 100f;
                filling = false;
            }
        } else {
            barProgress -= delta;
            if (barProgress <= 0f) {
                barProgress = 0f;
                filling = true;
            }
        }
    }

    @Override
    public void handleKey(String key) {
        if (isDone()) return;
        if (key.equals(" ")) {
            rounds++;
            if (barProgress >= 70f && barProgress <= 100f) {
                addProgress(20f);
                message = "　グッドタイミング！";
            } else {
                addProgress(-10f);
                message = "　タイミングが惜しい…";
            }
            barProgress = 0f;
            filling = true;
        }
    }

    @Override
    public boolean isDone() {
        return panel.progress >= 100f;
    }

    @Override
    public void renderOverlay(StringBuilder sb) {
        sb.append("\033[15;20H\033[96m");
        sb.append("┌──────────────────────────────────────────┐");
        sb.append("\033[16;20H│\033[0m\033[96m　　　タイミングミニゲーム　　　　　　　　　　│");
        sb.append("\033[17;20H│\033[0m　　　バーが赤ゾーンに入ったらスペースを押せ！　\033[96m│");

        // Bar
        int filled = (int)(barProgress / 10f);
        StringBuilder bar = new StringBuilder("　[");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                if (i >= 7) bar.append("\033[91m■\033[0m"); // red zone
                else bar.append("■");
            } else {
                bar.append("□");
            }
        }
        bar.append("]");
        sb.append(String.format("\033[18;20H│\033[0m%s　　　　　　　　　　　　　\033[96m│", bar.toString()));

        String progStr = String.format("　進捗：%5.1f%%　　ラウンド：%d/%d", panel.progress, rounds, MAX_ROUNDS);
        sb.append(String.format("\033[19;20H│\033[0m%s　　　　　\033[96m│", progStr));

        if (!message.isEmpty()) {
            sb.append(String.format("\033[20;20H│\033[0m%s　　　　　　　　　　　　　\033[96m│", message));
        } else {
            sb.append("\033[20;20H│\033[0m　　　　　　　　　　　　　　　　　　　　　　　\033[96m│");
        }
        sb.append("\033[21;20H│\033[0m　　　　　　　　　　　　　　　　　　　　　　　\033[96m│");
        sb.append("\033[22;20H│\033[0m　スペースでタイミング　ESC/Qで中断　　　　　　\033[96m│");
        sb.append("\033[23;20H└──────────────────────────────────────────┘\033[0m");
    }
}
