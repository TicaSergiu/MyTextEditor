import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Window {
    private final UnixTerminal terminal;
    private final List<String> content = new ArrayList<>();
    private final Cursor cursor;
    int rows;
    int cols;
    boolean running;
    int offsetY;

    public Window() {
        terminal = new UnixTerminal();
        this.running = true;
        this.cursor = new Cursor();
        this.offsetY = 0;
    }

    public Window(String path) {
        this();
        try {
            content.addAll(Files.readAllLines(Path.of(path)));
        } catch (IOException e) {
            exit(e.getMessage());
        }
    }

    private void exit(String msg) {
        System.err.println(msg);
        terminal.disableRawMode();
    }

    public void start() {
        terminal.enableRawMode();
        getSizeOfTerminal();

        while (running) {
            scroll();
            refresh();
            byte[] key = new byte[3];
            int length = getInput(key);
            handleInput(key, length);
        }
        terminal.disableRawMode();
        System.out.print(Keys.ESC + "2J");
        System.out.print(Keys.ESC + "H");
    }

    private void scroll() {
        if (cursor.getY() >= rows + offsetY) {
            offsetY = cursor.getY() - rows + 1;
        } else if (cursor.getY() < offsetY) {
            offsetY = cursor.getY();
        }
    }

    private void handleInput(byte[] key, int length) {
        if (length == 1) {
            if (key[0] == 'q') {
                running = false;
            } else if (key[0] == Keys.BACKSPACE) {
                removeChar();
            } else {
                System.out.print((char) key[0]);
            }
        } else {
            handleArrowKeys(key);
        }
    }

    private void removeChar() {
        if (cursor.getX() == 0) {
            return;
        }
        String str = content.remove(cursor.getY());
        content.add(cursor.getY(), str.substring(0, cursor.getX() - 1) + str.substring(cursor.getX()));
        cursor.moveLeft();
    }

    private void handleArrowKeys(byte[] code) {
        switch (code[2]) {
            // move cursor left
            case Keys.ARROW_LEFT -> {
                if (cursor.getX() > 0) {
                    cursor.moveLeft();
                }
            }
            // move cursor right
            case Keys.ARROW_RIGHT -> {
                if (content.isEmpty()) {
                    return;
                }
                if (cursor.getX() < content.get(cursor.getY())
                                           .length()) {
                    cursor.moveRight();
                }
            }
            // move cursor down
            case Keys.ARROW_DOWN -> {
                if (cursor.getY() < content.size() - 1) {
                    cursor.moveDown();
                }
            }
            // move cursor up
            case Keys.ARROW_UP -> {
                if (cursor.getY() > 0) {
                    cursor.moveUp();
                }
            }
            case Keys.END -> cursor.moveEnd(content.get(cursor.getY())
                                                   .length());
            case Keys.HOME -> cursor.moveHome();
            default -> throw new UnsupportedOperationException();
        }
    }

    private int getInput(byte[] key) {
        try {
            return System.in.read(key);
        } catch (IOException e) {
            exit(e.getMessage());
        }
        return 0;
    }

    private void refresh() {
        StringBuilder builder = new StringBuilder();

        builder.append(Keys.ESC + "H");
        drawLines(builder);
        drawStatusBar(builder);
        moveCursor(builder);

        System.out.print(builder);
    }

    private void moveCursor(StringBuilder builder) {
        // moves cursor to line #, column #
        String location = String.format(Keys.ESC + "%d;%dH", cursor.getY() - offsetY + 1, cursor.getX() + 1);
        builder.append(location);
    }

    private void drawStatusBar(StringBuilder builder) {
        String statusMsg = "MyTextEditor";
        String cursorPosition = String.format("Offset: %d Rows:%d Cols:%d x:%d y:%d", offsetY, rows, cols, cursor.getX() + 1, cursor.getY() + 1);
        builder.append(Keys.ESC + "7m")
               .append(statusMsg)
               .append(" ".repeat(Math.max(0, cols - statusMsg.length() - cursorPosition.length())))
               .append(cursorPosition)
               .append(Keys.ESC + "0m");
    }

    private void drawLines(StringBuilder builder) {
        for (int i = 0; i < rows; i++) {
            int line = i + offsetY;
            if (line < content.size()) {
                builder.append(content.get(line));
            } else {
                builder.append("~");
            }
            builder.append(Keys.ESC + "K\r\n");
        }
    }

    /**
     * This should be called AFTER the raw/cooked mode is enabled on the terminal.
     */
    private void getSizeOfTerminal() {
        WindowSize size = terminal.getWindowSize();
        this.rows = size.rows() - 1;
        this.cols = size.columns();
    }
}
