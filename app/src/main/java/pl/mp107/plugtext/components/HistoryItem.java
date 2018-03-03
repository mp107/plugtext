package pl.mp107.plugtext.components;

public class HistoryItem {

    private String text;
    private int cursorPostition;

    public HistoryItem() {
    }

    public HistoryItem(String text, int cursorPostition) {
        this.text = text;
        this.cursorPostition = cursorPostition;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCursorPostition() {
        return cursorPostition;
    }

    public void setCursorPostition(int cursorPostition) {
        this.cursorPostition = cursorPostition;
    }
}
