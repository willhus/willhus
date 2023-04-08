package server;

public class ServerLauncher {
    public final static int PORT = 1337;

    public static void main(String[] args) {
        Server server;
        try {
            server = new Server(PORT);
            System.out.println("Server is running...");
            String currentDir = System.getProperty("user.dir");
            System.out.println("Current directory: " + currentDir);

            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}