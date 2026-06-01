public class TypingMiniGame extends MiniGame {
    private static final String[] PROMPTS = {
        "prison", "light", "honor", "exit", "free",
        "run", "bolt", "trip", "lure", "rip"
    };
    private int promptIdx = 0;
    private String currentPrompt;
    private String inputBuffer = "";
    private String message = "";
    private int round = 0;
    private static final int MAX_ROUNDS = 5;

    public TypingMiniGame(SecurityPanel panel) {
        super(panel);
        promptIdx = (int)(Math.random() * PROMPTS.length);
        currentPrompt = PROMPTS[promptIdx];
    }

    private void nextPrompt() {
        promptIdx = (promptIdx + 1) % PROMPTS.length;
        currentPrompt = PROMPTS[promptIdx];
        inputBuffer = "";
    }

    @Override
    public void handleKey(String key) {
        if (isDone()) return;
        if (key.equals("") || key.equals("ENTER")) {
            round++;
            if (inputBuffer.equals(currentPrompt)) {
                addProgress(20f);
                message = "　正解！";
            } else {
                message = "　不正解… 正解：" + currentPrompt;
            }
            inputBuffer = "";
            if (!isDone()) nextPrompt();
        } else if (key.equals("BACKSPACE") || key.equals("\b")) {
            if (!inputBuffer.isEmpty()) inputBuffer = inputBuffer.substring(0, inputBuffer.length()-1);
        } else if (key.length() == 1) {
            char c = key.charAt(0);
            if (c >= 32 && c < 127 && inputBuffer.length() < 20) {
                inputBuffer += c;
            }
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
        sb.append("\033[16;20H│\033[0m\033[96m　　　タイピングミニゲーム　　　　　　　　　　│");
        sb.append("\033[17;20H│\033[0m　　　以下の文字列を正確に入力してEnterを押せ　\033[96m│");

        sb.append(String.format("\033[18;20H│\033[0m　　お題：%s　　　　　　　　　　　　　　　　　\033[96m│", currentPrompt));
        sb.append(String.format("\033[19;20H│\033[0m　　入力：%s＿　　　　　　　　　　　　　　　　\033[96m│", inputBuffer));

        String progStr = String.format("　進捗：%5.1f%%　　ラウンド：%d/%d", panel.progress, round, MAX_ROUNDS);
        sb.append(String.format("\033[20;20H│\033[0m%s　　　　　\033[96m│", progStr));

        if (!message.isEmpty()) {
            sb.append(String.format("\033[21;20H│\033[0m%s　　　　　　　　　　　　\033[96m│", message));
        } else {
            sb.append("\033[21;20H│\033[0m　　　　　　　　　　　　　　　　　　　　　　　\033[96m│");
        }
        sb.append("\033[22;20H│\033[0m　Enterで確定　ESC/Qで中断　　　　　　　　　　\033[96m│");
        sb.append("\033[23;20H└──────────────────────────────────────────┘\033[0m");
    }
}
