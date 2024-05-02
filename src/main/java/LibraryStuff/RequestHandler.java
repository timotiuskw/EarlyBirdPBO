package LibraryStuff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHandler implements HttpHandler {
    private String webDirectory;
    private String logDirectory;

    public RequestHandler(String webDirectory, String logDirectory) {
        this.webDirectory = webDirectory;
        this.logDirectory = logDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod(); // Mendapatkan Metode HTTP seperti GET, POST, PUT, dll.
        String requestPath = exchange.getRequestURI().getPath(); // Mengambil Path

        logAccess(exchange.getRemoteAddress().getAddress().getHostAddress(), requestPath); // Menulis Log berdasarkan Fungsi dibawah.

        // Menangani GET saja.
        if (requestMethod.equalsIgnoreCase("GET")) {
            handleGetRequest(exchange, requestPath);
        } else {
            sendErrorResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Method Not Allowed");
        }
    }

    private void handleGetRequest(HttpExchange exchange, String requestPath) throws IOException {
        // Menggabungkan Path WebDir dengan Path yang ingin kita buka (RequestPath)
        // Jika user mengakses http://localhost:8080/Files/14002_Tugas_4.pdf
        // Jadi nanti RequestPath nya adalah /Files/14002_Tugas_4.pdf
        Path filePath = Paths.get(webDirectory, requestPath.substring(1));
        File file = filePath.toFile(); // Path diubah menjadi file untuk mengetahui apakah itu direktori/file.
    
        if (file.exists() && file.isFile()) {
            if (file.getName().endsWith(".mp4")) {
                // Mengirim respons dengan tipe konten video
                exchange.getResponseHeaders().set("Content-Type", "video/mp4");
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());
                OutputStream outputStream = exchange.getResponseBody();
                Files.copy(file.toPath(), outputStream);
                outputStream.close();
            } else {
                // Jika bukan file video MP4, kirim respons standar
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());
                OutputStream outputStream = exchange.getResponseBody();
                Files.copy(file.toPath(), outputStream);
                outputStream.close();
            }
        } else if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            StringBuilder response = new StringBuilder(); // Lebih memory-efficient
            response.append("<html>");
            response.append("<head>")
                    .append("<meta charset=\"UTF-8\">")
                    .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                    .append("<script src=\"https://kit.fontawesome.com/c3b79002ab.js\" crossorigin=\"anonymous\"></script>")
                    .append("<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Poppins&display=swap\">")
                    .append("<title>Web Server</title>");
            response.append("<style>");
            response.append("body {");
            response.append("    font-family: 'Poppins', sans-serif;");
            response.append("}");
            response.append(".container {");
            response.append("    display: flex;");
            response.append("    flex-wrap: wrap;");
            response.append("    justify-content: space-evenly;");
            response.append("}");
            response.append(".item {");
            response.append("    width: 30%; /* Menggunakan 30% untuk memastikan 3 item per baris */");
            response.append("    margin-bottom: 20px;");
            response.append("    text-align: center;");
            response.append("    border: 1px solid black;");
            response.append("}");
            response.append(".item img {");
            response.append("    width: 100px; /* Atur ukuran gambar kecil di samping kiri item */");
            response.append("    display: inline-block;");
            response.append("    vertical-align: middle;");
            response.append("    margin-right: 10px;");
            response.append("}");
            response.append(".item h3 {");
            response.append("    display: inline-block;");
            response.append("    vertical-align: middle;");
            response.append("}");
            response.append("i {");
            response.append("    margin-right: 5px;");
            response.append("    font-size: 20px;");
            response.append("    vertical-align: middle;");
            response.append("}");
            response.append("h1 {");
            response.append("    text-align: center;");
            response.append("}");
            response.append(".item a {");
            response.append("    display: block;");
            response.append("    width: 100%;");
            response.append("    height: 100%;");
            response.append("    text-decoration: none; /* Hapus garis bawah default pada tautan */");
            response.append("    color: inherit; /* Gunakan warna teks default */");
            response.append("}");
            response.append("h3 {")
                    .append("    white-space: nowrap;")
                    .append("    overflow: hidden;")
                    .append("    text-overflow: ellipsis;")
                    .append("    max-width: 80%;")
                    .append("}");
            response.append("</style>");
            response.append("</head>");
            response.append("<body><h1>Index of ").append(requestPath).append("</h1>");
            response.append("<div class=\"container\">");

            // Jika Bukan di Halaman Root
            if (!requestPath.equals("/")) {
                response.append("<div class=\"item\">");
                response.append("    <a href=\"../\">");
                response.append("        <i class=\"fa-solid fa-folder\" style=\"color: #e4b611;\"></i>");
                response.append("        <h3>Back</h3>");
                response.append("    </a>");
                response.append("</div>");
                response.append("</body>");
                response.append("</html>");
                // response.append("<li><a href=\"../\">..   (Back)</a></li>");
            }

            for (File f : files) {
                // Mengecek Apakah Path File tersebut Direktori/File.
                String linkPath = f.isDirectory() ? requestPath + f.getName() + "/" : requestPath + f.getName();
                String liClass = f.isDirectory() ? "fa-folder" : "fa-file";

                response.append("<div class=\"item\">");
                response.append("    <a href=\"").append(linkPath).append("\">");

                if (liClass == "fa-folder")
                {
                    response.append("        <i class=\"fa-solid ").append(liClass).append("\" style=\"color: #e4b611;\"></i>");
                } else if (liClass == "fa-file" && f.getName().endsWith(".html"))
                {
                    response.append("        <i class=\"fa-brands fa-html5").append("\" style=\"color: #f50000;\"></i>");
                } else if (liClass == "fa-file" && f.getName().endsWith(".pdf"))
                {
                    response.append("        <i class=\"fa-solid fa-file-pdf").append("\" style=\"color: #f50000;\"></i>");
                } else if (liClass == "fa-file" && f.getName().endsWith(".css"))
                {
                    response.append("        <i class=\"fa-brands fa-css3-alt").append("\" style=\"color: #74C0FC;\"></i>");
                } else if (liClass == "fa-file" && (f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".png")))
                {
                    response.append("        <i class=\"fa-solid fa-image").append("\" style=\"color: #63E6BE;\"></i>");
                } else if (liClass == "fa-file" && f.getName().endsWith(".mp4"))
                {
                    response.append("        <i class=\"fa-solid fa-video").append("\" style=\"color: #B197FC;\"></i>");
                } else if (liClass == "fa-file" && f.getName().endsWith(".txt"))
                {
                    response.append("        <i class=\"fa-solid fa-file-lines").append("\" style=\"color: #74C0FC;\"></i>");
                } else
                {
                    response.append("        <i class=\"fa-solid ").append(liClass).append("\"></i>");
                }

                response.append("        <h3>").append(f.getName()).append("</h3>");
                response.append("    </a>");
                response.append("</div>");

                // response.append("<li").append(liClass).append("><a href=\"").append(linkPath).append("\">").append(f.getName()).append("</a></li>");
            }

            response.append("</div></body></html>");
            sendResponse(exchange, HttpURLConnection.HTTP_OK, response.toString());
        } else {
            // File Not Found
            sendErrorResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, "Not Found");
        }
    }    

    private void logAccess(String ipAddress, String requestPath) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String logFileName = dateFormat.format(new Date()) + ".log";
        String logFilePath = Paths.get(logDirectory, logFileName).toString();

        try {
            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            String logEntry = String.format("[%s] %s - %s\n", new Date(), ipAddress, requestPath);
            Files.write(Paths.get(logFilePath), logEntry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendResponse(exchange, statusCode, message);
    }
}
