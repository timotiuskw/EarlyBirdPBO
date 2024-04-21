package LibraryStuff;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    private int port;
    private String webDirectory;
    private String logDirectory;
    private HttpServer server;
    private boolean isRunning;

    public WebServer(int port, String webDirectory, String logDirectory) {
        this.port = port;
        this.webDirectory = webDirectory;
        this.logDirectory = logDirectory;
        this.isRunning = false;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RequestHandler(webDirectory, logDirectory));
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        this.isRunning = true;
        System.out.println("Server started on port " + port);
    }

    public void stop() {
        if (this.isRunning == true) {
            server.stop(0); // Stop the server with a delay of 0 milliseconds
            this.isRunning = false;
            System.out.println("Server stopped");
        } else
        {
            System.out.println("Server isn't running");
        }
    }

    public boolean isRunning(){
        return this.isRunning;
    }
}
