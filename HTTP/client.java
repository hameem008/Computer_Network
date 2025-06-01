import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class client {
    private static String dir = "./files/";
    private static String filePath = new String();
    private static String serverHost = "localhost";
    private static int serverPort = 5055;
    private static String fileName = new String();

    public static void main(String[] args) {
        try {
            while (true) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Write the name of the file: ");
                fileName = scanner.nextLine();
                filePath = dir + fileName;
                new uploader().start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class uploader extends Thread {
        public void run() {
            try {
                Socket socket = new Socket(serverHost, serverPort);
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                PrintWriter pr = new PrintWriter(outputStream);
                pr.println("UPLOAD " + fileName);
                pr.flush();
                try {
                    FileInputStream fiiInputStream = new FileInputStream(filePath);
                    File file = new File(filePath);
                    if (fileName.endsWith(".txt") || fileName.endsWith(".jpg") || fileName.endsWith(".png")
                            || fileName.endsWith(".mp4")) {
                        pr.println("ok");
                        pr.flush();
                        dataOutputStream.writeUTF(file.getName());
                        dataOutputStream.writeLong(file.length());
                        byte[] buffer = new byte[2048];
                        int bytesRead;
                        while ((bytesRead = fiiInputStream.read(buffer)) > 0) {
                            dataOutputStream.write(buffer, 0, bytesRead);
                        }
                    } else {
                        pr.println("Invalid format.");
                        pr.flush();
                        System.out.println("Invalid format.");
                    }
                    fiiInputStream.close();
                } catch (Exception e) {
                    pr.println("Invalid filename.");
                    pr.flush();
                    System.out.println("Invalid filename.");
                }
                dataOutputStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
