package com.anil.todolist;

import dataModel.TodoData;
import dataModel.TodoItem;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;


public class DialogController {

public TextField shortDescriptionField;
public TextArea detailsField;
public DatePicker deadLineField;

 public TodoItem processResult()
 {
     String shortDescription=shortDescriptionField.getText().trim();
     String details=detailsField.getText();
     LocalDate date=deadLineField.getValue();
     TodoItem item=new TodoItem(shortDescription,details,date);
      TodoData.getInstance().addTodoItem(item);
      return item;
 }
}
