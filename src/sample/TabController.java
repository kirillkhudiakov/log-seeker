package sample;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.*;

public class TabController {

    @FXML AnchorPane mainPane;
    @FXML TextField rootDirectoryText;
    @FXML TextField userText;
    @FXML ListView<File> searchList;
    @FXML Button searchButton;
    @FXML TextField extensionField;
    @FXML TreeView<String> treeView;
    @FXML ListView<String> listView;
    ObservableList<String> listItems;

    ExecutorService executor;
    private File root;
    private String displayedPath;
    private ExecutorService fileListingExecutor;
    private ExecutorService fileSeekerExecutor;
    private static final int LIST_SIZE = 20;

    @FXML
    public void initialize() {
        String home = System.getProperty("user.home");
        rootDirectoryText.setText(home);
        root = new File(home);
        treeView.getSelectionModel().selectedItemProperty().addListener(this::onFileClicked);
        listItems = FXCollections.observableArrayList();
        listView.setItems(listItems);
        fileSeekerExecutor = Executors.newSingleThreadExecutor();
        fileListingExecutor = Executors.newSingleThreadExecutor();

        listView.setOnScroll((scrollEvent -> {
            System.out.println("Scroll");
        }));
    }

    void onFileClicked(ObservableValue<? extends TreeItem<String>> observableValue, TreeItem<String> oldValue, TreeItem<String> newValue) {
        if (newValue == null)
            return;

        fileListingExecutor.shutdownNow();
        try {
            if (!fileListingExecutor.awaitTermination(1, TimeUnit.SECONDS))
                throw new TimeoutException();
        } catch (TimeoutException | InterruptedException e) {
            System.out.println(e.toString());
        }

        String path = getPath(newValue);
        File file = new File(path);
        listItems.clear();
        if (file.isDirectory())
            return;

        fileListingExecutor = Executors.newSingleThreadExecutor();
        fileListingExecutor.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(path))){
                String line = reader.readLine();
                for (int i = 0; i < LIST_SIZE && line != null; ++i) {
                    final String copy = line;
                    Platform.runLater(() -> listItems.add(copy));
                    line = reader.readLine();
                }
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        });
    }

    @FXML
    private void onKeyTyped() {
        if (userText.getText().equals("") && !searchButton.isDisable())
            searchButton.setDisable(true);
        else if (!userText.getText().equals("") && searchButton.isDisable())
            searchButton.setDisable(false);
    }

    @FXML
    private void onSelectDirClicked() {
        DirectoryChooser chooser = new DirectoryChooser();
        Stage primaryStage = (Stage) mainPane.getScene().getWindow();
        root = chooser.showDialog(primaryStage);
        if (root != null)
            rootDirectoryText.setText(root.toString());
    }

    @FXML
    private void onSearchClicked() {
//        try {
        treeView.setRoot(null);
        searchButton.setDisable(true);
            new Thread(() -> {
                Seeker seeker = new Seeker(root, userText.getText(), extensionField.getText());
                ForkJoinPool pool = new ForkJoinPool();
                pool.invoke(seeker);
                boolean result = seeker.join();
                if (result) {
                    Platform.runLater(() -> {
                        treeView.setRoot(seeker.getTreeItem());
                        displayedPath = root.getPath();
                    });
                }
                searchButton.setDisable(false);

//                ExecutorService executor = Executors.newCachedThreadPool();
//                Seeker seeker = new Seeker(executor);
//                ConcurrentLinkedQueue<File> result = seeker.find(root, userText.getText(), extensionField.getText());
//
//                System.out.println("Print");
//                searchList.setItems(FXCollections.observableArrayList(result));
            }).start();
//        } catch (Exception e) {
//            System.out.println(e.toString());
//        }
    }

    private String getPath(TreeItem<String> item) {
        TreeItem<String> parent;
        StringBuilder stringBuilder = new StringBuilder();
        while ((parent = item.getParent()) != null) {
            stringBuilder.insert(0, item.getValue()).insert(0, "\\");
            item = parent;
        }
        return stringBuilder.insert(0, displayedPath).toString();
    }
}
