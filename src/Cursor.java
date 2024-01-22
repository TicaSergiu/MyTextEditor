public class Cursor {
    private int x;
    private int y;

    public Cursor() {
        this.x = 0;
        this.y = 0;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x += 1;
    }

    public void moveUp() {
        y--;
    }

    public void moveDown() {
        y++;
    }

    public void moveHome() {
        x = 0;
    }

    public void moveEnd(int lineLength) {
        x = lineLength;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
