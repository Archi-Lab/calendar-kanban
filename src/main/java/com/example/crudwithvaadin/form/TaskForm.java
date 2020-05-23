package com.example.crudwithvaadin.form;

import authentication.CurrentUser;
import com.example.crudwithvaadin.view.TaskViewImpl;
import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.example.crudwithvaadin.repository.CategoryRepository;
import com.example.crudwithvaadin.repository.TaskRepository;

import java.time.LocalDate;

public class TaskForm extends Div {

    private final TaskViewImpl view;
    private TextField title = new TextField("Kurztitel");
    private ComboBox categoryBox = new ComboBox("Kategorie");
    private ComboBox columnBox = new ComboBox("Zeitpunkt");
    private ComboBox sizeBox = new ComboBox("Size");
    private DatePicker datum = new DatePicker("Wann erledigt?(Optional)");
    private Button save = new Button("Speichern");
    private Button abort = new Button("Abbrechen");
    private Button delete = new Button("Löschen");
    private VerticalLayout content;
    private TaskRepository repository;
    private CategoryRepository categoryRepository;
    private Task task = null;

    public TaskForm(TaskRepository repo,CategoryRepository categoryRepository, TaskViewImpl view){
        this.repository=repo;
        this.categoryRepository=categoryRepository;
        this.view=view;
        this.sizeBox.setItems(Task.Size.values());
        this.sizeBox.setValue(Task.Size.M);
        this.columnBox.setItems(Task.Priority.values());
        this.columnBox.setValue(Task.Priority.TODAY);
        this.columnBox.setAllowCustomValue(false);
        categoryBox.setItems(categoryRepository.findByOwner(CurrentUser.getRole()));
        categoryBox.setItemLabelGenerator(new ItemLabelGenerator() {
            @Override
            public String apply(Object o) {
                if (o instanceof Category){
                    return ((Category) o).getBeschreibung();
                }
                return "";
            }
        });
        setClassName("product-form");
        content = new VerticalLayout();
        content.setSizeUndefined();
        add(content);

        abort.addClickListener(e->{
           this.setVisible(false);
        });
        abort.setWidth("100%");

        save.setWidth("100%");
        save.addClickListener(e->{
            if(task!=null){
                task.setTitle(title.getValue());
                task.setDueDate(datum.getValue());
                task.setCreationDate(LocalDate.now());
                task.setColumn((Task.Priority) columnBox.getValue());
                task.setSize((Task.Size) sizeBox.getValue());
                task.setCategory((Category) categoryBox.getValue());
                repository.save(task);
                this.view.refreshGridData();
            }else{
                Task newTask = new Task();
                newTask.setTitle(title.getValue());
                newTask.setDueDate(datum.getValue());
                newTask.setCreationDate(LocalDate.now());
                newTask.setColumn((Task.Priority) columnBox.getValue());
                newTask.setCategory((Category) categoryBox.getValue());
                newTask.setUser(CurrentUser.getRole());
                newTask.setSize((Task.Size) sizeBox.getValue());
                repository.save(newTask);
                this.view.refreshGridData();
            }
            task=null;
            this.setVisible(false);
        });
        delete.addClickListener(e->{
            deleteTask();
        });
        delete.setWidth("100%");
        content.add(title,sizeBox, columnBox,categoryBox,datum,save,delete,abort);
    }

    private void deleteTask() {
        if(task!=null){
            repository.delete(task);
            this.view.refreshGridData();
            task=null;
        }
        this.setVisible(false);
    }

    public void fillForm(Task task){
        this.task=task;
        if(task!=null) {
            title.setValue(task.getTitle());
            datum.setValue(task.getDueDate());
            columnBox.setValue(task.getColumn());
            sizeBox.setValue(task.getSize());
            categoryBox.setValue(task.getCategory());
        }else{
            title.setValue("");
            datum.setValue(null);
            sizeBox.setValue(null);
            columnBox.setValue(null);
            categoryBox.setValue(null);
        }
    }

}
