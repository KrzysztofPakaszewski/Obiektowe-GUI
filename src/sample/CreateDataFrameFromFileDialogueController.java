package sample;

import Data.Values.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


public class CreateDataFrameFromFileDialogueController {

    @FXML
    public Button addColumnID;
    @FXML
    public Button deleteColumnID;
    @FXML
    public Button okButton;
    @FXML
    public Button cancelButton;
    public TableColumn<DialogueTable, TextField> nameCol;
    public TableColumn<DialogueTable, ComboBox> typeCol;
    public TableView tableView;
    public CheckBox checkHeader;

    private Controller ref;


    ObservableList<DialogueTable> list = FXCollections.observableArrayList();

    public void addColumn(ActionEvent actionEvent) {
        add();
    }

    public void deleteColumn(ActionEvent actionEvent) {
        Object selectedRow =tableView.getSelectionModel().getSelectedItem();
        tableView.getItems().remove(selectedRow);
    }

    public void okClicked(ActionEvent actionEvent) {
        if(checkIfTypesAndNamesAreSet()) {
            ref.header = checkHeader.isSelected();
            for (int a = 0; a < list.size(); a++) {
                ref.types.add(list.get(a).getType().getValue());
                if (!ref.header) {
                    ref.names.add(list.get(a).getName().getText());
                }
            }
            Stage tmp = (Stage) okButton.getScene().getWindow();
            tmp.close();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("An Error Occurred");
            alert.setContentText("Types or names are not set!");
            alert.showAndWait();
        }

    }

    private boolean checkIfTypesAndNamesAreSet() {
        for(int a=0;a< list.size();a++){
            if(list.get(a).getType().getValue()==null){
                return false;
            }
            if(!checkHeader.isSelected() && list.get(a).getName().getText().equals("")){
                return false;
            }
        }
        return true;
    }

    public void cancelClicked(ActionEvent actionEvent) {
        Stage tmp =(Stage) cancelButton.getScene().getWindow();
        ref.filePath="";
        tmp.close();

    }
    public void init(Controller ref){
        this.ref=ref;
    }
    public void add(){
        ComboBox<Class<? extends Value>> type = new ComboBox<>();
        type.getItems().add(IntegerValue.class);
        type.getItems().add(StringValue.class);
        type.getItems().add(FloatValue.class);
        type.getItems().add(DoubleValue.class);
        type.getItems().add(DateTimeValue.class);
        TextField name= new TextField();
        list.add(new DialogueTable(name,type));
        tableView.setItems(list);
        nameCol.setCellValueFactory(new PropertyValueFactory<DialogueTable,TextField>("name"));
        typeCol.setCellValueFactory(new PropertyValueFactory<DialogueTable,ComboBox>("type"));
    }

    public void Checked(ActionEvent actionEvent) {
        if(checkHeader.isSelected()) {
            nameCol.visibleProperty().setValue(false);
        }
        else{
            nameCol.visibleProperty().setValue(true);
        }
    }
}
