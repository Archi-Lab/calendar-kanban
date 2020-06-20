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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

public class TaskForm extends Div {

    private final TaskViewImpl view;
    private TextField title = new TextField("Titel");
    private ComboBox<Category> categoryBox = new ComboBox("Category");
    private ComboBox<Task.Priority> columnBox = new ComboBox("Column");
    private ComboBox<Task.Size> sizeBox = new ComboBox("Size");
    private DatePicker deadlineField = new DatePicker("Deadline(Optional)");
    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;
    private Task task = null;

    public TaskForm(TaskRepository repo, CategoryRepository categoryRepository, TaskViewImpl view){
        this.taskRepository =repo;
        this.view=view;
        this.categoryRepository=categoryRepository;
        this.buildLayout();
    }

    private void buildLayout() {
        this.sizeBox.setItems(Task.Size.values());
        this.columnBox.setItems(Task.Priority.values());
        this.columnBox.setAllowCustomValue(false);
        this.categoryBox.setItems(categoryRepository.findByOwner(CurrentUser.getUser()));
        this.categoryBox.setItemLabelGenerator(new ItemLabelGenerator() {
            @Override
            public String apply(Object o) {
                if (o instanceof Category){
                    return ((Category) o).getBeschreibung();
                }
                return "";
            }
        });
        title.setRequired(true);
        categoryBox.setRequired(true);
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
        content.add(title,sizeBox, columnBox,categoryBox, deadlineField, save, abort, delete);
    }

    private void saveTaskEvent(ClickEvent<Button> buttonClickEvent) {
        if(title.getValue().trim().length()==0||categoryBox.getValue()==null){
            new Notification("Werte nicht komplett befüllt", 2000).open();
            return;
        }
        if(task!=null){
            task.setTitle(title.getValue());
            task.setDueDate(deadlineField.getValue());
            task.setCreationDate(LocalDate.now());
            task.setColumn(columnBox.getValue());
            task.setSize(sizeBox.getValue());
            task.setCategory((Category) categoryBox.getValue());
            taskRepository.save(task);
            this.view.refreshGridData();
        }else{
            Task newTask = new Task();
            newTask.setTitle(title.getValue());
            newTask.setDueDate(deadlineField.getValue());
            newTask.setCreationDate(LocalDate.now());
            newTask.setColumn(columnBox.getValue());
            newTask.setCategory((Category) categoryBox.getValue());
            newTask.setUser(CurrentUser.getUser());
            newTask.setSize(sizeBox.getValue());
            taskRepository.save(newTask);
            this.view.refreshGridData();
        }
        task=null;
        this.setVisible(false);
    }

    private void deleteTask() {
        if(task!=null){
            taskRepository.delete(task);
            this.view.refreshGridData();
            task=null;
        }
        this.setVisible(false);
    }

    public void fillForm(Task task){
        this.categoryBox.setItems(this.categoryRepository.findByOwner(CurrentUser.getUser()));
        this.task=task;
        if(task!=null) {
            title.setValue(task.getTitle());
            deadlineField.setValue(task.getDueDate());
            columnBox.setValue(task.getColumn());
            sizeBox.setValue(task.getSize());
            categoryBox.setValue(task.getCategory());
        }else{
            title.setValue("");
            deadlineField.setValue(null);
            this.sizeBox.setValue(Task.Size.M);
            categoryBox.setValue(null);
            this.columnBox.setValue(Task.Priority.TODAY);
        }
    }

}
