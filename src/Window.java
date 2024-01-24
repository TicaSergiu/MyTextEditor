import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Window {
    private final UnixTerminal terminal;
    private final List<String> content;
    private final Cursor cursor;
    int rows;
    int cols;
    boolean running;
    int offsetY;
    private Mode currentMode;

    public Window() {
        this.terminal = new UnixTerminal();
        this.content = new ArrayList<>();
        this.running = true;
        this.cursor = new Cursor();
        this.offsetY = 0;
        this.currentMode = Mode.NORMAL;
    }

    public Window(String path) {
        this();
        try {
            content.addAll(Files.readAllLines(Path.of(path)));
        } catch (IOException e) {
            exit(e.getMessage());
        }
    }

    private static void reset() {
        System.out.print(EscapeCode.CLEAR_SCREEN);
        System.out.print(EscapeCode.MOVE_CURSOR_TOP_LEFT);
        System.out.print(EscapeCode.BLOCK_CURSOR);
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
        reset();
    }

    private void scroll() {
        if (cursor.getY() >= rows + offsetY) {
            offsetY = cursor.getY() - rows + 1;
        } else if (cursor.getY() < offsetY) {
            offsetY = cursor.getY();
        }
    }

    private void handleInput(byte[] key, int length) {
        if (key[0] == ctrl('q')) {
            running = false;
            return;
        }
        if (currentMode == Mode.NORMAL) {
            handleInputNormalMode(key);
        } else {
            handleInputInsertMode(key, length);
        }
    }

    private void handleInputInsertMode(byte[] key, int length) {
        if (length == 1) {
            if (key[0] == Keys.BACKSPACE) {
                deleteChar();
            } else if (key[0] == Keys.ESCAPE) {
                currentMode = Mode.NORMAL;
                System.out.print(EscapeCode.BLOCK_CURSOR);
            } else {
                System.out.print((char) key[0]);
            }
        } else {
            handleArrowKeys(key);
        }
    }

    private int ctrl(char c) {
        return c & 0x1f;
    }

    private void handleInputNormalMode(byte[] key) {
        if (!Set.of('h', 'j', 'k', 'l', 'i', Keys.END, Keys.HOME, 'I')
                .contains((char) key[0])) {
            return;
        }
        switch (key[0]) {
            case 'h' -> {
                key[2] = Keys.ARROW_LEFT;
                handleArrowKeys(key);
            }
            case 'j' -> {
                key[2] = Keys.ARROW_DOWN;
                handleArrowKeys(key);
            }
            case 'k' -> {
                key[2] = Keys.ARROW_UP;
                handleArrowKeys(key);
            }
            case 'l' -> {
                key[2] = Keys.ARROW_RIGHT;
                handleArrowKeys(key);
            }
            case Keys.END -> {
                key[2] = Keys.END;
                handleArrowKeys(key);
            }
            case Keys.HOME -> {
                key[2] = Keys.HOME;
                handleArrowKeys(key);
            }
            case 'i' -> {
                currentMode = Mode.INSERT;
                System.out.print(EscapeCode.BEAM_CURSOR);
            }
            case 'I' -> {
                currentMode = Mode.INSERT;
                System.out.print(EscapeCode.BEAM_CURSOR);
                key[2] = Keys.HOME;
                handleArrowKeys(key);
            }
            default -> {
            }
        }
    }

    private void deleteChar() {
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

        builder.append(EscapeCode.MOVE_CURSOR_TOP_LEFT);
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

    enum Mode {
        NORMAL,
        INSERT
    }
}
