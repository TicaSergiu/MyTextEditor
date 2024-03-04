import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GapBuffer {
    private final int GAP_SIZE = 50;
    byte[] text;
    private int length;
    private int cursorLeft;
    private int cursorRight;

    public GapBuffer() {
        text = new byte[256];
        cursorLeft = cursorRight = 0;
    }

    public GapBuffer(String path) {
        try {
            text = Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        cursorLeft = cursorRight = 0;
    }

    public void append(byte inputText) {
        cursorLeft++;
        if (cursorLeft == cursorRight) {
            grow();
        }

        text[cursorLeft] = inputText;
    }

    private void grow() {
        byte[] copy = new byte[text.length + GAP_SIZE];

        System.arraycopy(text, 0, copy, 0, cursorLeft);
        System.arraycopy(text, cursorLeft + GAP_SIZE, copy, cursorLeft + GAP_SIZE, text.length - cursorLeft);
    }
}
