public class EscapeCode {
    private static final String ESC = "\033[";
    public static final String MOVE_CURSOR_TOP_LEFT = ESC + "H";
    public static final String CLEAR_SCREEN = ESC + "2J";
    public static final String BEAM_CURSOR = ESC + "5 q";
    public static final String BLOCK_CURSOR = ESC + "1 q";
}
