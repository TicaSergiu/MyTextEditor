public class Main {
    public static void main(String[] args) {
        Window window;
        if (args.length == 1) {
            window = new Window(args[0]);
        } else {
            window = new Window("examples/py.txt");
        }
        window.start();
    }
}