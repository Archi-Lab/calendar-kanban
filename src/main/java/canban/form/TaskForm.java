package canban.form;

import authentication.CurrentUser;
import canban.entity.Category;
import canban.entity.Task;
import canban.repository.CategoryRepository;
import canban.repository.TaskRepository;
import canban.view.TaskViewImpl;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

public class TaskForm extends Div {

    private final TaskViewImpl view;
    private TextField title = new TextField("Titel");
    private ComboBox<Object> categoryBox = new ComboBox("Category");
    private ComboBox<Task.Priority> columnBox = new ComboBox("Column");
    private ComboBox<Task.Size> sizeBox = new ComboBox("Size");
    private DatePicker datum = new DatePicker("Deadline(Optional)");
    private TaskRepository repository;
    private Task task = null;

    public TaskForm(TaskRepository repo, CategoryRepository categoryRepository, TaskViewImpl view){
        this.repository=repo;
        this.view=view;
        this.sizeBox.setItems(Task.Size.values());
        this.columnBox.setItems(Task.Priority.values());
        this.columnBox.setAllowCustomValue(false);
        categoryBox.setItems(categoryRepository.findByOwner(CurrentUser.getRole()));
        categoryBox.setItemLabelGenerator((ItemLabelGenerator) o -> {
            if (o instanceof Category){
                return ((Category) o).getBeschreibung();
            }
            return "";
        });
        setClassName("product-form");
        VerticalLayout content = new VerticalLayout();
        content.setSizeUndefined();
        add(content);

        Button abort = new Button("Abort");
        abort.addClickListener(e-> this.setVisible(false));
        abort.setWidth("100%");

        Button save = new Button("Save");
        save.setWidth("100%");
        save.addClickListener(this::saveTaskEvent);
        save.addClickShortcut(Key.KEY_S, KeyModifier.ALT);

        Button delete = new Button("Delete");
        delete.addClickListener(e-> deleteTask());
        delete.setWidth("100%");
        delete.addClassName("deleteBtn");
        delete.setIcon(VaadinIcon.TRASH.create());
        delete.addClickShortcut(Key.KEY_D, KeyModifier.ALT);
        content.add(title,sizeBox, columnBox,categoryBox,datum, save, abort, delete);
    }

    private void saveTaskEvent(ClickEvent<Button> buttonClickEvent) {
        if(task!=null){
            task.setTitle(title.getValue());
            task.setDueDate(datum.getValue());
            task.setCreationDate(LocalDate.now());
            task.setColumn(columnBox.getValue());
            task.setSize(sizeBox.getValue());
            task.setCategory((Category) categoryBox.getValue());
            repository.save(task);
            this.view.refreshGridData();
        }else{
            Task newTask = new Task();
            newTask.setTitle(title.getValue());
            newTask.setDueDate(datum.getValue());
            newTask.setCreationDate(LocalDate.now());
            newTask.setColumn(columnBox.getValue());
            newTask.setCategory((Category) categoryBox.getValue());
            newTask.setUser(CurrentUser.getRole());
            newTask.setSize(sizeBox.getValue());
            repository.save(newTask);
            this.view.refreshGridData();
        }
        task=null;
        this.setVisible(false);
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
            this.sizeBox.setValue(Task.Size.M);
            categoryBox.setValue(null);
            this.columnBox.setValue(Task.Priority.TODAY);
        }
    }

}
