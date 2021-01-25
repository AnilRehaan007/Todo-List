package com.anil.todolist;

import dataModel.TodoData;
import dataModel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {
    public List<TodoItem> todoItems;
    public ListView<TodoItem> todoListView;
    public TextArea itemDetailsTextArea;
     public Label deadlineLabel;
     public BorderPane mainBorderPane;
  public ContextMenu contextMenu;
  public ToggleButton filterTodoList;
  public FilteredList<TodoItem> filteredList;
  public Predicate<TodoItem> wantAllItems;
    public Predicate<TodoItem> todayItems;
    public void initialize() {

        contextMenu=new ContextMenu();
        MenuItem deleteMenuItem=new MenuItem("delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item=todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        contextMenu.getItems().addAll(deleteMenuItem);
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
                if(newValue != null) {
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("(MMM-d-y)");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });

        wantAllItems=new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return true;
            }
        };
        todayItems=new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return (item.getDeadline().equals(LocalDate.now()));
            }
        };
      filteredList=new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(),wantAllItems);
        SortedList<TodoItem> sortedList=new SortedList<TodoItem>(filteredList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });
//        todoListView.setItems(TodoData.getInstance().getTodoItems());
        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell=new ListCell<TodoItem>()
                {
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean empty) {
                        super.updateItem(todoItem, empty);
                        if(empty)
                        {
                            setText(null);
                        }
                        else
                        {
                            setText(todoItem.getShortDescription());
                            if(todoItem.getDeadline().isBefore(LocalDate.now().plusDays(1)))
                            {
                                setTextFill(Color.RED);
                            }
                            else if(todoItem.getDeadline().equals(LocalDate.now().plusDays(1)))
                            {
                                setTextFill(Color.BROWN);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs,wasEmpty,isEmpty)->{
                            if(isEmpty)
                            {
                                cell.setContextMenu(null);
                            }
                            else
                            {
                                cell.setContextMenu(contextMenu);
                            }
                        }
                );
                return cell;
            }
        });

    }

public void onKeyPressed(KeyEvent keyEvent)
{
    TodoItem item=todoListView.getSelectionModel().getSelectedItem();
    if(item!=null)
    {
        if(keyEvent.getCode().equals(KeyCode.DELETE))
        {
            deleteItem(item);
        }
    }

}
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("detail corner");
        FXMLLoader fxmlLoader=new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoitemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result=dialog.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK)
        {
            DialogController controller=fxmlLoader.getController();
            TodoItem item=controller.processResult();

            todoListView.getSelectionModel().select(item);

        }
    }
   public void deleteItem(TodoItem item)
   {
       Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
       alert.setTitle("delete todoItem");
       alert.setHeaderText("delete item: " + item.getShortDescription());
       alert.setContentText("are you sure you want to delete");
       Optional<ButtonType> result=alert.showAndWait();
       if(result.isPresent() && result.get()==ButtonType.OK)
       {
           TodoData.getInstance().deleteTodoItem(item);
       }
   }
   public void handleFilterButton()
   {
        TodoItem item=todoListView.getSelectionModel().getSelectedItem();
       if(filterTodoList.isSelected())
       {
           filteredList.setPredicate(todayItems);
           if(filteredList.isEmpty())
           {
               itemDetailsTextArea.clear();
               deadlineLabel.setText("");
           }
           else if(filteredList.contains(item))
           {
             todoListView.getSelectionModel().select(item);
           }
           else
               todoListView.getSelectionModel().selectFirst();
       }
       else
       {
           filteredList.setPredicate(wantAllItems);
           todoListView.getSelectionModel().select(item);
       }

   }

    public void handleExit()
    {
        Platform.exit();
    }
}
