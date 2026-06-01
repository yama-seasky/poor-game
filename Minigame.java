import java.util.Random;

public class Minigame {
    public static final int TYPE_MATH = SecurityDevice.TYPE_MATH;
    public static final int TYPE_ZC_ALT = SecurityDevice.TYPE_ZC_ALT;
    public static final int TYPE_TIMING = SecurityDevice.TYPE_TIMING;
    public static final int TYPE_TYPING = SecurityDevice.TYPE_TYPING;

    public int type;
    public boolean completed;
    public boolean failed;

    // MATH
    public int mathQuestion; // 0-9
    public int mathA, mathB;
    public int mathOp; // 0=+, 1=-, 2=*
    public String mathInput;
    public String mathFeedback;
    public Random rng;

    // ZC_ALT
    public int zcProgress; // 0-19
    public boolean zcExpectZ; // true=expect Z, false=expect C

    // TIMING
    public int timingRound; // 0-4
    public int timingCursor; // 0-29
    public int timingCursorDir; // 1 or -1
    public int timingSafeZone; // 0-24 (start of 5-wide safe zone)
    public boolean timingHit;
    public int timingTick;
    public boolean timingRoundActive;
    public int timingMisses;

    // TYPING
    public static final String[] TYPING_WORDS = {"tiger", "pickle", "zonk", "cubic", "river"};
    public int typingWord; // 0-4
    public String typingInput;
    public String typingFeedback;

    public Minigame(int type) {
        this.type = type;
        this.completed = false;
        this.failed = false;
        this.rng = new Random();
        reset();
    }

    public void reset() {
        completed = false;
        failed = false;
        switch (type) {
            case TYPE_MATH:
                mathQuestion = 0;
                mathInput = "";
                mathFeedback = "";
                generateMathQuestion();
                break;
            case TYPE_ZC_ALT:
                zcProgress = 0;
                zcExpectZ = true;
                break;
            case TYPE_TIMING:
                timingRound = 0;
                timingCursor = 0;
                timingCursorDir = 1;
                timingSafeZone = rng.nextInt(25);
                timingHit = false;
                timingTick = 0;
                timingRoundActive = true;
                timingMisses = 0;
                break;
            case TYPE_TYPING:
                typingWord = 0;
                typingInput = "";
                typingFeedback = "";
                break;
        }
    }

    public void generateMathQuestion() {
        mathA = 10 + rng.nextInt(90);
        mathB = 10 + rng.nextInt(90);
        mathOp = rng.nextInt(3);
        if (mathOp == 1 && mathB > mathA) {
            int tmp = mathA; mathA = mathB; mathB = tmp;
        }
        mathInput = "";
        mathFeedback = "";
    }

    public int getMathAnswer() {
        switch (mathOp) {
            case 0: return mathA + mathB;
            case 1: return mathA - mathB;
            case 2: return mathA * mathB;
        }
        return 0;
    }

    public String getMathOpStr() {
        switch (mathOp) {
            case 0: return "＋";
            case 1: return "－";
            case 2: return "×";
        }
        return "＋";
    }

    // Returns true if minigame is done (completed or failed)
    public boolean processInput(String key) {
        switch (type) {
            case TYPE_MATH: return processMath(key);
            case TYPE_ZC_ALT: return processZC(key);
            case TYPE_TIMING: return processTiming(key);
            case TYPE_TYPING: return processTyping(key);
        }
        return false;
    }

    private boolean processMath(String key) {
        if (key.equals("ESC")) {
            return true; // cancel, save progress
        }
        if (key.length() == 1) {
            char c = key.charAt(0);
            if (c >= '0' && c <= '9') {
                if (mathInput.length() < 8) mathInput += c;
            } else if (c == 8 || key.equals("BACKSPACE")) {
                if (!mathInput.isEmpty()) mathInput = mathInput.substring(0, mathInput.length() - 1);
            }
        }
        if (key.isEmpty()) { // ENTER
            if (!mathInput.isEmpty()) {
                try {
                    int answer = Integer.parseInt(mathInput);
                    if (answer == getMathAnswer()) {
                        mathQuestion++;
                        mathFeedback = "せいかい！";
                        if (mathQuestion >= 10) {
                            completed = true;
                            return true;
                        }
                        generateMathQuestion();
                    } else {
                        mathFeedback = "ちがいます！";
                        mathInput = "";
                    }
                } catch (NumberFormatException e) {
                    mathFeedback = "ちがいます！";
                    mathInput = "";
                }
            }
        }
        return false;
    }

    private boolean processZC(String key) {
        if (key.equals("ESC")) {
            return true; // cancel
        }
        if (key.equals("z") || key.equals("Z")) {
            if (zcExpectZ) {
                zcProgress++;
                zcExpectZ = false;
                if (zcProgress >= 20) { completed = true; return true; }
            } else {
                if (zcProgress > 0) zcProgress--;
            }
        } else if (key.equals("c") || key.equals("C")) {
            if (!zcExpectZ) {
                zcProgress++;
                zcExpectZ = true;
                if (zcProgress >= 20) { completed = true; return true; }
            } else {
                if (zcProgress > 0) zcProgress--;
            }
        }
        return false;
    }

    public void tickTiming() {
        if (type != TYPE_TIMING || !timingRoundActive || completed || failed) return;
        timingTick++;
        if (timingTick % 2 == 0) {
            timingCursor += timingCursorDir;
            if (timingCursor >= 30) { timingCursor = 29; timingCursorDir = -1; }
            if (timingCursor < 0) { timingCursor = 0; timingCursorDir = 1; }
        }
    }

    private boolean processTiming(String key) {
        if (key.equals("ESC")) {
            return true; // cancel
        }
        if (key.equals(" ")) {
            if (timingRoundActive) {
                // Check if cursor in safe zone
                if (timingCursor >= timingSafeZone && timingCursor < timingSafeZone + 5) {
                    // Hit!
                    timingRound++;
                    if (timingRound >= 5) { completed = true; return true; }
                    // New round
                    timingCursor = 0;
                    timingCursorDir = 1;
                    timingSafeZone = rng.nextInt(25);
                    timingTick = 0;
                } else {
                    // Miss - reset cursor for retry
                    timingMisses++;
                    timingCursor = 0;
                    timingCursorDir = 1;
                }
                timingRoundActive = true;
            }
        }
        return false;
    }

    private boolean processTyping(String key) {
        if (key.equals("ESC")) {
            return true; // cancel
        }
        if (key.isEmpty()) { // ENTER
            String target = TYPING_WORDS[typingWord];
            if (typingInput.equals(target)) {
                typingWord++;
                typingFeedback = "せいかい！";
                typingInput = "";
                if (typingWord >= 5) { completed = true; return true; }
            } else {
                typingFeedback = "ちがいます！もう一度";
                typingInput = "";
            }
        } else if (key.length() == 1) {
            char c = key.charAt(0);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                if (typingInput.length() < 20) typingInput += Character.toLowerCase(c);
            }
        }
        return false;
    }
}
