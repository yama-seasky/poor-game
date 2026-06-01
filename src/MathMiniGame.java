public class MathMiniGame extends MiniGame {
    private int a, b;
    private char op;
    private int answer;
    private String inputBuffer = "";
    private String message = "";
    private int problemsAnswered = 0;

    public MathMiniGame(SecurityPanel panel) {
        super(panel);
        generateProblem();
    }

    private void generateProblem() {
        a = 10 + (int)(Math.random() * 90);
        b = 10 + (int)(Math.random() * 90);
        int opIdx = (int)(Math.random() * 3);
        if (opIdx == 0) { op = '+'; answer = a + b; }
        else if (opIdx == 1) { op = '-'; answer = a - b; }
        else { op = '×'; answer = a * b; }
        inputBuffer = "";
        message = "";
    }

    @Override
    public void handleKey(String key) {
        if (isDone()) return;
        if (key.equals("") || key.equals("ENTER")) {
            // submit
            try {
                int val = Integer.parseInt(inputBuffer.trim());
                if (val == answer) {
                    addProgress(10f);
                    problemsAnswered++;
                    message = "　正解！";
                } else {
                    addProgress(-10f);
                    message = "　不正解… 正解：" + answer;
                }
            } catch (NumberFormatException e) {
                message = "　数字を入力してください";
            }
            inputBuffer = "";
            if (!isDone()) generateProblem();
        } else if (key.equals("BACKSPACE") || key.equals("\b")) {
            if (!inputBuffer.isEmpty()) inputBuffer = inputBuffer.substring(0, inputBuffer.length()-1);
        } else if (key.length() == 1) {
            char c = key.charAt(0);
            if ((c >= '0' && c <= '9') || (c == '-' && inputBuffer.isEmpty())) {
                if (inputBuffer.length() < 8) inputBuffer += c;
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
        sb.append("\033[16;20H│\033[0m\033[96m　　　数学ミニゲーム　　　　　　　　　　　　　│");
        sb.append("\033[17;20H│\033[0m　　　　　　　　　　　　　　　　　　　　　　　\033[96m│");

        String progressStr = String.format("　進捗：%5.1f%%", panel.progress);
        sb.append(String.format("\033[18;20H│\033[0m%s　　　　　　　　　　　　　\033[96m│", progressStr));

        String problem = String.format("　問題：%d %c %d ＝ ？", a, op, b);
        sb.append(String.format("\033[19;20H│\033[0m%s　　　　　　　　　　\033[96m│", problem));

        String input = "　入力：" + inputBuffer + "＿";
        sb.append(String.format("\033[20;20H│\033[0m%s　　　　　　　　　　　　　\033[96m│", input));

        if (!message.isEmpty()) {
            sb.append(String.format("\033[21;20H│\033[0m%s　　　　　　　　　　　　\033[96m│", message));
        } else {
            sb.append("\033[21;20H│\033[0m　　　　　　　　　　　　　　　　　　　　　　　\033[96m│");
        }

        sb.append("\033[22;20H│\033[0m　Enterで確定　ESC/Qで中断　　　　　　　　　　\033[96m│");
        sb.append("\033[23;20H└──────────────────────────────────────────┘\033[0m");
    }
}
