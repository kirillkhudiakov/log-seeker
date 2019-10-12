package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class MainController {

    @FXML public Tab newTab;
    @FXML public TabPane tabPane;

    private int counter = 1;

    @FXML
    public void onNewTab() throws IOException {
        if (newTab.isSelected()) {
            Tab tab = new Tab("Tab " + counter++, FXMLLoader.load(getClass().getResource("sample.fxml")));
            tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);
            tabPane.getSelectionModel().select(tab);
        }
    }
}
