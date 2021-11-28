package com.geekbrains.chat.client;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CloudController implements Initializable {

    private final File directory = new File("src/main/resources/com/geekbrains/cloud/");
    public ListView<String> listView; // список файлов в директории клиента
    public Button sendFile;
    private IoNet net;
    private List<String> list;
    private Set<String> selectedList;
    private ObservableList<String> items;

/*    public void sendMsg(ActionEvent actionEvent) throws IOException {
        net.sendMsg(input.getText());
        input.clear();
        // String item = listView.getSelectionModel().getSelectedItem();
        // отправить выбранный в listView файл на сервер
        // придумать как это сделать

    }

    private void addMessage(String msg) {
        // Platform.runLater(() -> listView.getItems().add(msg));
    }*/


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            net = new IoNet(socket);

            // обрабатываем список файлов для удобного представления
            List<String> dirs = Arrays.stream(Objects.requireNonNull(directory.list()))
                    .map(m -> new File(directory + "\\" + m))
                    .map(n -> {
                        if (n.isDirectory()) {
                            return "[Dir ]" + n;
                        } else {
                            return "[file]" + n + "\t\t" + convertTime(n.lastModified()) + " " + convertFileSize(n);
                        }
                    })
                    .sorted()
                    .map(o -> o.substring(0, 6) + o.substring(o.lastIndexOf("\\") + 1))
                    //.peek(System.out::println)
                    .collect(Collectors.toList());


            // собираем выделенные файлы в коллекцию для отправки на сервер
            list = Stream.of(Objects.requireNonNull(directory.list()))
                    .collect(Collectors.toList());
            selectedList = new HashSet<>();
            items = FXCollections.observableArrayList(dirs);
            listView.setItems(items);
            listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listView.getSelectionModel().getSelectedItems().addListener(
                    (ListChangeListener.Change<? extends String> change) ->
                    {
                        ObservableList<String> oList = listView.getSelectionModel().getSelectedItems();
                        selectedList.addAll(oList);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // отправляем коллекцию с выбранными файлами в метод отправки на сервер
    public void sendFileToCloud(ActionEvent actionEvent) throws IOException {
        for (String str : selectedList) {
            net.sendFile(str.substring(6, str.indexOf("\t")));
        }
        selectedList.clear();
    }

    // изменяем время последней модификации файла в удобный формат
    private String convertTime(long t) {
        Date date = new Date(t);
        Format format = new SimpleDateFormat("HH:mm dd.MM.yy");
        return format.format(date);
    }

    // конвертируем размер файла в удобный формат
    private String convertFileSize(File file) {
        long size = 0;
        try {
            size = Files.size(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (size < 1024) {
            return String.format("%,d b", size);
        } else {
            return String.format("%,d kb", size / 1024);
        }
    }
}
