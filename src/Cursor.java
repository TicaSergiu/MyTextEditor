public class Cursor {
    private int x;
    private int y;
    private int snappingX;

    public Cursor() {
        this.x = 0;
        this.y = 0;
        this.snappingX = 0;
    }

    public void moveLeft() {
        x--;
        snappingX = x;
    }

    public void moveRight() {
        x++;
        snappingX = x;
    }

    public void moveUp(int nextStringLength) {
        y--;
        if (x > nextStringLength) {

        }
    }

    public void moveDown(int nextStringLength) {
        y++;
        if (x > nextStringLength) {
            x = nextStringLength;
        } else {
            x = snappingX;
        }
    }

    public void moveHome() {
        snappingX = x = 0;
    }

    public void moveEnd(int lineLength) {
        snappingX = x = lineLength;
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
