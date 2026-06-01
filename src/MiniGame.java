public abstract class MiniGame {
    protected SecurityPanel panel;

    public MiniGame(SecurityPanel panel) {
        this.panel = panel;
    }

    public abstract void handleKey(String key);
    public abstract boolean isDone();
    public abstract void renderOverlay(StringBuilder sb);
    public abstract void update(long dt);

    protected float getProgress() {
        return panel.progress;
    }

    protected void addProgress(float amount) {
        panel.progress = Math.min(100f, Math.max(0f, panel.progress + amount));
    }
}
