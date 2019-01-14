package sample;

import Data.DataFrame;
import Data.Values.DateTimeValue;
import Data.Values.StringValue;
import Data.Values.Value;
import Data.exceptions.CannotCreateValueFromString;
import Data.exceptions.Error;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    public ComboBox xAxis;
    public ComboBox yAxis;
    public Button createChartButton;
    public Slider slider;
    public Label rowValue;
    public Button updateStats;
    public TableColumn<StatTable,String> colnameCol;
    public TableColumn<StatTable,String> maxCol;
    public TableColumn<StatTable,String> minCol;
    public TableColumn<StatTable,String> meanCol;
    public TableColumn<StatTable,String> stdCol;
    public TableColumn<StatTable,String> varCol;
    public TableView statTableId;
    public TableView dataTable;
    public ComboBox<String> chartType;

    private int start=0;
    private int step=20;
    ObservableList<ArrayList<String>> items= FXCollections.observableArrayList();
    ObservableList<ArrayList<String>> bigdata= FXCollections.observableArrayList();

    ObservableList<StatTable> statList= FXCollections.observableArrayList();
    DataFrame data;
    ArrayList<Class<? extends Value>> types = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    boolean header;
    String filePath= new String();
    @FXML
    public MenuItem ImportFileButton;

    public void ImportFromFile(ActionEvent actionEvent) {
        dataTable.getColumns().clear();
        items.clear();
        bigdata.clear();
        start=0;
        filePath="";
        types.clear();
        names.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Data", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePath = new String(selectedFile.getAbsolutePath());
            try {
                openDialogue();
                if(filePath!="") {
                    createDataFrame();
                    setupDataTable();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success!");
                    alert.setHeaderText("Success!");
                    alert.setContentText("DataFrame has been successfully imported");
                    alert.showAndWait();
                }

            } catch (Exception e) {
                exceptionThrown(e);
                types.clear();
                names.clear();
                filePath="";
            }

        }
    }

    private void openDialogue() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreateDataFrameFromFileDialogue.fxml"));
        Parent parent = fxmlLoader.load();
        CreateDataFrameFromFileDialogueController dialogueController = fxmlLoader.getController();
        dialogueController.init(this);
        dialogueController.add();

        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void createDataFrame() throws Error, CannotCreateValueFromString,IOException {
        if(header){
            data = new DataFrame(filePath,types);
            names=data.getNames();
        }
        else{
            data= new DataFrame(filePath,names.toArray(new String[0]),types);
        }
        for(int a=0; a< data.getNames().size();a++){
            yAxis.getItems().add(data.getNames().get(a));
            xAxis.getItems().add(data.getNames().get(a));
        }
        slider.setMax(data.size());
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                rowValue.setText(String.valueOf(newValue.intValue()));
            }
        });
        chartType.getItems().addAll("Line Chart","Bar Chart","Scatter Chart");
        updateStats.setDisable(false);
        xAxis.setDisable(false);
        yAxis.setDisable(false);
        createChartButton.setDisable(false);
        slider.setDisable(false);
        chartType.setDisable(false);
    }
    void setupDataTable() throws Error{
        for(int a=0;a< types.size();a++){
            TableColumn<ArrayList<String>,String> column= new TableColumn();
            column.setText(names.get(a));
            final int i=a;
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(i)));
            dataTable.getColumns().add(column);
        }
        int a=0;
        int size=20;
        if(data.size()<20){
            size=data.size();
        }
        for(;a<size;a++){
            ArrayList<String> row=new ArrayList<>();
            for(int b =0; b< types.size();b++){
                row.add(data.get(a,b).toString());
            }
            items.add(row);
        }
        for(;a< data.size();a++){
            ArrayList<String> row=new ArrayList<>();
            for(int b =0; b< types.size();b++){
                row.add(data.get(a,b).toString());
            }
            bigdata.add(row);
        }
        dataTable.setItems(items);
        ScrollBar scrollBar= (ScrollBar) dataTable.lookup(".scroll-bar:vertical");
        scrollBar.valueProperty().addListener((observable,oldValue,newValue) ->{
            if(newValue.doubleValue() >= scrollBar.getMax()){
                double target=newValue.doubleValue()*items.size();
                if(start+step > bigdata.size()){
                    if(start < bigdata.size()) {
                        items.addAll(bigdata.subList(start, bigdata.size() - 1));
                        start = bigdata.size();
                    }
                }
                else {
                    items.addAll(bigdata.subList(start,start+step));
                    start+=step;
                }
                scrollBar.setValue(target/items.size());

            }
        } );
    }

    public void createChart(ActionEvent actionEvent){
        try {
            if(xAxis.getValue()==null || yAxis.getValue()==null){
                throw new Exception("columns not selected");
            }
            StackPane stackPane = new StackPane();
            Scene scene = new Scene(stackPane);
            String X = xAxis.getValue().toString();
            String Y = yAxis.getValue().toString();
            int IndexX = data.getNames().indexOf(X);
            int IndexY = data.getNames().indexOf(Y);
            final Axis xaxis;
            final Axis yaxis;
            boolean isXString=types.get(IndexX).isInstance(new DateTimeValue()) ||
                    types.get(IndexX).isInstance(new StringValue(""));
            boolean isYString =types.get(IndexY).isInstance(new DateTimeValue())||
                    types.get(IndexY).isInstance(new StringValue(""));
            if(isXString) {
                xaxis= new CategoryAxis();
            }
            else
            {
                xaxis = new NumberAxis();
            }
            if(isYString) {
                yaxis= new CategoryAxis();
            }
            else
            {
                yaxis = new NumberAxis();
            }
            final Chart chart;
            if(chartType.getValue().toString().equals("Line Chart")){
                chart= new LineChart(xaxis,yaxis);
            }
            else if(chartType.getValue().toString().equals("Bar Chart")){
                chart= new BarChart(xaxis,yaxis);
            }
            else
                chart= new ScatterChart(xaxis,yaxis);
            XYChart.Series series = new XYChart.Series();
            series.setName("Data chart");
            int limit = (int) slider.getValue();
            xaxis.setLabel(X);
            yaxis.setLabel(Y);
            for (int a = 0; a < limit; a++) {
                XYChart.Data tmp=new XYChart.Data<>();
                if(isXString){
                    tmp.setXValue(data.get(a,IndexX).toString());
                }
                else
                {
                    tmp.setXValue(Double.parseDouble(data.get(a,IndexX).toString()));
                }
                if(isYString){
                    tmp.setYValue(data.get(a,IndexY).toString());
                }
                else
                {
                    tmp.setYValue(Double.parseDouble(data.get(a,IndexY).toString()));
                }
                series.getData().add(tmp);
            }
            ((XYChart) chart).getData().add(series);
            stackPane.getChildren().add(chart);
            Stage window = new Stage();
            window.setTitle("Chart");
            window.setScene(scene);
            window.show();
        }
        catch (Exception e){
            e.printStackTrace();
            exceptionThrown(e);
        }
    }

    public void updateStatistics(ActionEvent actionEvent){
        try {
            ArrayList<DataFrame> tmp = new ArrayList<>();
            tmp.add(data);
            DataFrame.Grouped grouped = new DataFrame.Grouped(tmp, "");
            for (int a = 0; a < types.size(); a++) {
                statList.add(new StatTable());
                statList.get(a).setColname(names.get(a));
            }
            DataFrame temp = grouped.max();
            for (int a = 0; a < types.size(); a++) {
                statList.get(a).setMax(temp.get(0, a).toString());
            }
            temp = grouped.min();
            for (int a = 0; a < types.size(); a++) {
                statList.get(a).setMin(temp.get(0, a).toString());
            }
            temp = grouped.mean();
            StringValue str = new StringValue("");
            DateTimeValue dt = new DateTimeValue();
            int i = 0;
            for (int a = 0; a < types.size(); a++) {
                if (types.get(a).isInstance(str) || types.get(a).isInstance(dt)) {
                    statList.get(a - i).setMean("");
                    i++;
                } else
                    statList.get(a).setMean(temp.get(0, a - i).toString());
            }
            temp = grouped.std();
            i = 0;
            for (int a = 0; a < types.size(); a++) {
                if (types.get(a).isInstance(str) || types.get(a).isInstance(dt)) {
                    statList.get(a - i).setStd("");
                    i++;
                } else
                    statList.get(a).setStd(temp.get(0, a - i).toString());
            }
            temp = grouped.var();
            i = 0;
            for (int a = 0; a < types.size(); a++) {
                if (types.get(a).isInstance(str) || types.get(a).isInstance(dt)) {
                    statList.get(a - i).setVar("");
                    i++;
                } else
                    statList.get(a).setVar(temp.get(0, a - i).toString());
            }
            statTableId.setItems(statList);
            colnameCol.setCellValueFactory(new PropertyValueFactory<StatTable, String>("colname"));
            maxCol.setCellValueFactory(new PropertyValueFactory<StatTable, String>("max"));
            minCol.setCellValueFactory(new PropertyValueFactory<StatTable, String>("min"));
            stdCol.setCellValueFactory(new PropertyValueFactory<StatTable, String>("std"));
            varCol.setCellValueFactory(new PropertyValueFactory<StatTable, String>("var"));
            meanCol.setCellValueFactory(new PropertyValueFactory<StatTable, String>("mean"));
        }
        catch (Exception e){
            exceptionThrown(e);
            statList.clear();
            colnameCol.getColumns().clear();
            maxCol.getColumns().clear();
            minCol.getColumns().clear();
            stdCol.getColumns().clear();
            varCol.getColumns().clear();
            meanCol.getColumns().clear();
        }

    }

    void exceptionThrown(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText("An Error Occurred");
        alert.setContentText(e.getLocalizedMessage());
        alert.showAndWait();
    }
}
