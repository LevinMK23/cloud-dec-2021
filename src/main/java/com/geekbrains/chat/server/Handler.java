package com.geekbrains.chat.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Handler implements Runnable {

    private final byte[] buf;
    private final InputStream is;
    private final OutputStream os;
    private final Socket socket;
    private boolean running;

    public Handler(Socket socket) throws IOException {
        running = true;
        buf = new byte[8192];
        this.socket = socket;
        is = socket.getInputStream();
        os = socket.getOutputStream();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            String temp = "";
            boolean isNewFile = false;
            while (running) {
                // вкрутить логику с получением файла от клиента
                // ищем во входящем потоке символ "$" и парсим название файла
                int read = is.read(buf);
                String message = new String(buf, 0, read);
                if (message.contains("$") && !isNewFile) {
                    temp = "src\\main\\resources\\com\\geekbrains\\storage\\" + message.substring(0, message.lastIndexOf("$"));
                    isNewFile = true;
                    continue;
                }
                // записываем на диск новый файл с пришедшим от клиента названием и наполняем его
                if (isNewFile) {
                    File file = new File(temp);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(buf, 0, read);
                    fos.close();
                    isNewFile = false;
                    continue;
                }
                if (message.equals("quit")) {
                    os.write("Client disconnected\n".getBytes(StandardCharsets.UTF_8));
                    close();
                    break;
                }
                System.out.println("Received: " + message);
                os.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() throws IOException {
        os.close();
        is.close();
        socket.close();
    }
}
