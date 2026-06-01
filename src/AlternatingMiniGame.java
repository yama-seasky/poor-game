public class AlternatingMiniGame extends MiniGame {
    private int count = 0;
    private static final int TARGET = 20;
    private boolean expectZ = true; // alternates Z -> C -> Z ...
    private String message = "";

    public AlternatingMiniGame(SecurityPanel panel) {
        super(panel);
        // Reset progress for this minigame type
    }

    @Override
    public void handleKey(String key) {
        if (isDone()) return;
        String upper = key.toUpperCase();
        if (expectZ && upper.equals("Z")) {
            count++;
            expectZ = false;
            addProgress(100f / TARGET);
            message = "　ＺとＣを交互に！";
        } else if (!expectZ && upper.equals("C")) {
            count++;
            expectZ = true;
            addProgress(100f / TARGET);
            message = "　ＺとＣを交互に！";
        } else if (upper.equals("Z") || upper.equals("C")) {
            message = "　順番が違う！";
        }
    }

    @Override
    public boolean isDone() {
        return panel.progress >= 100f;
    }

    @Override
    public void update(long dt) {}

    @Override
    public void renderOverlay(StringBuilder sb) {
        sb.append("\033[15;20H\033[96m");
        sb.append("┌──────────────────────────────────────────┐");
        sb.append("\033[16;20H│\033[0m\033[96m　　　交互入力ミニゲーム　　　　　　　　　　　│");
        sb.append("\033[17;20H│\033[0m　　　　　　　　　　　　　　　　　　　　　　　\033[96m│");
        sb.append(String.format("\033[18;20H│\033[0m　　ＺとＣを交互に%dかい押せ！（%d/%d）　　　\033[96m│", TARGET, count, TARGET));

        String gauge = buildGauge();
        sb.append(String.format("\033[19;20H│\033[0m　%s　　　　　　　　　　　　\033[96m│", gauge));

        String nextKey = expectZ ? "次のキー：Ｚ" : "次のキー：Ｃ";
        sb.append(String.format("\033[20;20H│\033[0m　%s　　　　　　　　　　　　　　　　\033[96m│", nextKey));

        if (!message.isEmpty()) {
            sb.append(String.format("\033[21;20H│\033[0m%s　　　　　　　　　　　　　\033[96m│", message));
        } else {
            sb.append("\033[21;20H│\033[0m　　　　　　　　　　　　　　　　　　　　　　　\033[96m│");
        }
        sb.append("\033[22;20H│\033[0m　ESC/Qで中断　　　　　　　　　　　　　　　　\033[96m│");
        sb.append("\033[23;20H└──────────────────────────────────────────┘\033[0m");
    }

    private String buildGauge() {
        int filled = (int)(panel.progress / 10f);
        StringBuilder g = new StringBuilder("進捗：[");
        for (int i = 0; i < 10; i++) g.append(i < filled ? "■" : "□");
        g.append("]");
        return g.toString();
    }
}
