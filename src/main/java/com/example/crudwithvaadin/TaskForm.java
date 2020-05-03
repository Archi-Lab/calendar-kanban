package com.example.crudwithvaadin;

import authentication.CurrentUser;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

public class TaskForm extends Div {

    private final TaskView view;
    private TextField beschreibung = new TextField("Beschreibung");
    private ComboBox categoryBox = new ComboBox("Kategorie");
    private Checkbox isImportant = new Checkbox("Wichtiger Termin?");
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

    public TaskForm(TaskRepository repo,CategoryRepository categoryRepository, TaskView view){
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
                task.setBeschreibung(beschreibung.getValue());
                task.setImportant(isImportant.getValue());
                task.setDueDate(datum.getValue());
                task.setCreationDate(LocalDate.now());
                task.setColumn((Task.Priority) columnBox.getValue());
                task.setSize((Task.Size) sizeBox.getValue());
                task.setCategory((Category) categoryBox.getValue());
                repository.save(task);
                this.view.refresh();
            }else{
                Task newTask = new Task();
                newTask.setBeschreibung(beschreibung.getValue());
                newTask.setImportant(isImportant.getValue());
                newTask.setDueDate(datum.getValue());
                newTask.setCreationDate(LocalDate.now());
                newTask.setColumn((Task.Priority) columnBox.getValue());
                newTask.setCategory((Category) categoryBox.getValue());
                newTask.setUser(CurrentUser.getRole());
                newTask.setSize((Task.Size) sizeBox.getValue());
                repository.save(newTask);
                this.view.refresh();
            }
            task=null;
            this.setVisible(false);
        });
        delete.addClickListener(e->{
            deleteTask();
        });
        delete.setWidth("100%");
        content.add(beschreibung,isImportant,sizeBox, columnBox,categoryBox,datum,save,delete,abort);
    }

    private void deleteTask() {
        if(task!=null){
            repository.delete(task);
            this.view.refresh();
            task=null;
        }
        this.setVisible(false);
    }

    public void fillForm(Task task){
        this.task=task;
        if(task!=null) {
            beschreibung.setValue(task.getBeschreibung());
            isImportant.setValue(task.isImportant());
            datum.setValue(task.getDueDate());
            columnBox.setValue(task.getColumn());
            sizeBox.setValue(task.getSize());
            categoryBox.setValue(task.getCategory());
        }else{
            beschreibung.setValue("");
            isImportant.setValue(false);
            datum.setValue(null);
            sizeBox.setValue(null);
            columnBox.setValue(null);
            categoryBox.setValue(null);
        }
    }

}
