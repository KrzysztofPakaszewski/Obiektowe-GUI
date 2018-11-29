package sample;

import Data.Values.Value;
import javafx.scene.control.ComboBox;

public class DialogueTable {
    public DialogueTable(javafx.scene.control.TextField name, ComboBox<Class<? extends Value>> type){
        this.name=name;
        this.type=type;
    }
    public javafx.scene.control.TextField getName() {
        return name;
    }

    public void setName(javafx.scene.control.TextField name) {
        this.name = name;
    }

    public ComboBox<Class<? extends Value>> getType() {
        return type;
    }

    public void setType(ComboBox<Class<? extends Value>> type) {
        this.type = type;
    }

    ComboBox<Class<? extends Value>> type;
    javafx.scene.control.TextField name;
}
