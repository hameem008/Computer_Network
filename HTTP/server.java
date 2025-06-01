import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class server {
    private static final int PORT = 5055;
    private static final String DIRECTORY_PATH = ".";
    private static final int chunkSize = 2048;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.setOut(new PrintStream(new FileOutputStream("log.txt")));
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private String directoryPath;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        private static String getFileContentType(Path filePath) {
            try {
                return Files.probeContentType(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                StringBuffer recieveRequest = new StringBuffer();
                int bytesRead;
                while ((bytesRead = inputStream.read()) != -1) {
                    if (bytesRead == '\n')
                        break;
                    recieveRequest.append((char) bytesRead);
                }
                PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
                String request = recieveRequest.toString();
                String[] requestStrings = request.split(" ");
                if (requestStrings.length > 1)
                    directoryPath = requestStrings[1];
                System.out.println("Request: ");
                System.out.println(request);
                if (request != null && request.startsWith("GET")) {
                    System.out.println("Response: ");
                    fileList(pr);
                } else if (request != null && request.startsWith("UPLOAD")) {
                    System.out.println("Response: ");
                    StringBuffer recieveReview = new StringBuffer();
                    while ((bytesRead = inputStream.read()) != -1) {
                        if (bytesRead == '\n')
                            break;
                        recieveReview.append((char) bytesRead);
                    }
                    String review = recieveReview.toString();
                    if (review.startsWith("ok")) {
                        uploadHandler();
                    } else {
                        System.out.println(review);
                    }
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void fileList(PrintWriter pr) {
            File directory = new File(DIRECTORY_PATH + directoryPath);
            File[] filesList = directory.listFiles();
            if (directory.isDirectory()) {
                StringBuffer response = new StringBuffer();
                response.append("HTTP/1.0 200 OK\r\n");
                response.append("Content-Type: text/html\r\n");
                response.append("\r\n");
                response.append("<html><head><title>File List</title></head><body>\r\n");
                response.append("<h1>Files in Directory</h1>\r\n");
                response.append("<ul>\r\n");
                if (filesList.length > 0) {
                    for (File file : filesList) {
                        if (file.isFile()) {
                            response.append(
                                    "<li>" + "<a href = " + directoryPath + file.getName() + ">" + file.getName()
                                            + "</a>" + "</li>\r\n");
                        } else if (file.isDirectory()) {
                            response.append(
                                    "<li><b><i>" + "<a href = " + directoryPath + file.getName() + "/>"
                                            + file.getName()
                                            + "</a>" + "</i></b></li>\r\n");
                        }
                    }
                } else {
                    response.append("<li>No files found.</li>\r\n");
                }
                response.append("</ul>\r\n");
                response.append("</body></html>\r\n");
                pr.println(response.toString());
                System.out.println(response);
            } else if (directory.isFile()) {
                String[] pathStrings = directoryPath.split("/");
                String fileName = pathStrings[pathStrings.length - 1];
                if (fileName.contains(".txt")
                        || fileName.contains(".jpg")
                        || fileName.contains(".png")) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(directory);
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        String contentType = getFileContentType(directory.toPath());
                        StringBuffer response = new StringBuffer();
                        response.append("HTTP/1.0 200 OK\r\n");
                        response.append("Content-Type: " + contentType + "\r\n");
                        response.append("Content-Length: " + directory.length() + "\r\n");
                        response.append("\r\n");
                        dataOutputStream.write(response.toString().getBytes());
                        dataOutputStream.flush();
                        byte[] buffer = new byte[chunkSize];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            dataOutputStream.write(buffer, 0, bytesRead);
                            dataOutputStream.flush();
                        }
                        fileInputStream.close();
                        dataOutputStream.close();
                        System.out.println(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(directory);
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        String contentType = getFileContentType(directory.toPath());
                        StringBuffer response = new StringBuffer();
                        response.append("HTTP/1.0 200 OK\r\n");
                        response.append("Content-Type: " + contentType + "\r\n");
                        response.append("Content-Length: " + directory.length() + "\r\n");
                        response.append("Content-Disposition: attachment; filename=\"" + fileName + "\"\r\n");
                        response.append("\r\n");
                        dataOutputStream.write(response.toString().getBytes());
                        byte[] buffer = new byte[chunkSize];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            dataOutputStream.write(buffer, 0, bytesRead);
                            dataOutputStream.flush();
                        }
                        fileInputStream.close();
                        dataOutputStream.close();
                        System.out.println(response);
                        System.out.println("File downloaded successfully.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                StringBuffer response = new StringBuffer();
                response.append("HTTP/1.0 404 Not Found\r\n");
                response.append("Content-Type: text/html\r\n");
                response.append("\r\n");
                response.append("<!DOCTYPE html>\r\n");
                response.append("<title>404 - Page Not Found</title></head>\r\n");
                response.append("<body style=\"text-align:center;\">\r\n");
                response.append("<h1>404 - Page Not Found</h1>\r\n");
                response.append("<p><a href=\"/\">Go back to the homepage</a></p>\r\n");
                response.append("</body></html>\r\n");
                pr.println(response.toString());
                System.out.println(response);
                System.out.println("Page not found.");
            }
        }

        private void uploadHandler() {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                String fileName = dataInputStream.readUTF();
                long fileSize = dataInputStream.readLong();
                System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");
                File file = new File(DIRECTORY_PATH + "/uploaded", fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[chunkSize];
                int bytesRead;
                long totalBytesRead = 0;
                while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                fileOutputStream.close();
                dataInputStream.close();
                System.out.println("File " + fileName + " uploaded successfully (" + totalBytesRead + " bytes).\r\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}