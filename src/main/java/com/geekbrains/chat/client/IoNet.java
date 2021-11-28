package com.geekbrains.chat.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class IoNet implements Closeable {

    //private final Callback callback;
    private final Socket socket;
    private final InputStream is;
    private final OutputStream os;
    private final byte[] buf;

    public IoNet(
            Socket socket) throws IOException {
        //this.callback = callback;
        this.socket = socket;
        is = socket.getInputStream();
        os = socket.getOutputStream();
        buf = new byte[1000000];
/*        Thread readThread = new Thread(this::readMessages);
        readThread.setDaemon(true);
        readThread.start();*/
    }

    public void sendMsg(String msg) throws IOException {
        os.write(msg.getBytes(StandardCharsets.UTF_8));
        os.flush();
    }


    // метод отправки файла в облако, отправляем имя файла, потом отправляем сам файл
    public void sendFile(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream("src\\main\\resources\\com\\geekbrains\\cloud\\" + fileName);
        int read;
        os.write((fileName + "$").getBytes(StandardCharsets.UTF_8));
        while ((read = fis.read(buf)) != -1) {
            os.write(buf, 0, read);
        }
        os.flush();
    }

/*    private void readMessages() {
        try {
            while (true) {
                int read = is.read(buf);
                String msg = new String(buf, 0, read).trim();
                callback.onReceive(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public void close() throws IOException {
        os.close();
        is.close();
        socket.close();
    }
}
