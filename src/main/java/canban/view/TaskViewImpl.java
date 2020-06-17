package canban.view;

import authentication.AccessControlFactory;
import authentication.CurrentUser;
import canban.component.ColumnGrid;
import canban.controller.TaskViewController;
import canban.entity.Category;
import canban.entity.Task;
import canban.entity.User;
import canban.repository.CategoryRepository;
import canban.repository.TaskRepository;
import canban.form.TaskForm;
import canban.repository.UserRepository;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.*;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CssImport("./styles/style.css")
@HtmlImport(value = "./html/file.html")
@Route("Termin")
public class TaskViewImpl extends VerticalLayout implements TaskView {

    //Components
    private final TextField input = new TextField("Search");
    private final ComboBox<Object> categoryBox = new ComboBox<>("Category");
    private final ComboBox<ColumnGrid.Order> orderBox = new ComboBox<>("Sort");
    private final Button creatTermin = new Button("Task");
    private final Button settingsBtn = new Button();
    private final Button ascdescButton = new Button();
    private final Button adminView = new Button("Adminmask");
    private final Checkbox multiselectBox = new Checkbox("Multiselect");
    private final Checkbox doneShow = new Checkbox("Show Done");
    private ColumnGrid<Task> laterGridToday;
    private ColumnGrid<Task> nextNweekGridTomorrow;
    private ColumnGrid<Task> nextWeekGridThisWeek;
    private ColumnGrid<Task> currentWeekGridThisWeek;
    private ColumnGrid<Task> todayGridThisWeek;
    private ColumnGrid<Task> nearlyDoneGridThisWeek;
    private ColumnGrid<Task> donenGridThisWeek;

    //Controller
    private TaskViewController controller;

    //Forms
    private TaskForm form;


    //Other Variables
    private ColumnGrid<Task> dragSource = null;
    private List<Task> draggedItems = null;
    private Category category = null;
    private List<ColumnGrid<Task>> gridList = new ArrayList<>();
    private boolean isInit = false;
    private boolean isASC = true;

    public TaskViewImpl(TaskRepository repo, CategoryRepository categoryRepository, UserRepository userRepository) {
        if (CurrentUser.getRole() != null){
            this.controller = new TaskViewController(this,repo,categoryRepository,userRepository);
            this.controller.onEnter();
        }
    }

    @Override
    public void configListener() {
        ComponentEventListener<GridDragStartEvent<Task>> dragStartListener = event -> {
            draggedItems = event.getDraggedItems();
            dragSource = (ColumnGrid<Task>) event.getSource();
        };
        ComponentEventListener<GridDragEndEvent<Task>> dragEndListener = event -> {
            draggedItems = null;
            dragSource = null;
        };
        ComponentEventListener<GridDropEvent<Task>> dropListener = event -> {
            Optional<Task> target = event.getDropTargetItem();
            if (target.isPresent() && draggedItems.contains(target.get())) {
                return;
            }

            ColumnGrid<Task> targetGrid = (ColumnGrid<Task>) event.getSource();
            draggedItems.forEach(e-> e.setColumn(targetGrid.getPriority()));
            this.controller.saveTasks(draggedItems);

            dragSource.refreshContainer(category,input.getValue(), this.orderBox.getValue(),isASC);
            targetGrid.refreshContainer(category,input.getValue(), this.orderBox.getValue(),isASC);
        };

        for (ColumnGrid<Task> grid :gridList){
            grid.addDragStartListener(dragStartListener);
            grid.addDragEndListener(dragEndListener);
            grid.addDropListener(dropListener);
            grid.addItemDoubleClickListener(e-> this.controller.taskClicked(e.getItem()));
        }
        this.multiselectBox.addValueChangeListener(e-> gridList.forEach(grid-> grid.setSelectionMode(e.getValue()?Grid.SelectionMode.MULTI: Grid.SelectionMode.SINGLE)));

        this.doneShow.addValueChangeListener(e-> this.donenGridThisWeek.setVisibleItems(e.getValue(),this.category,this.input.getValue(), this.orderBox.getValue(),isASC));

        this.input.setValueChangeMode(ValueChangeMode.LAZY);
        this.input.addValueChangeListener(e-> refreshGridData());

        this.creatTermin.addClickListener(e-> this.controller.taskClicked(null));
        this.creatTermin.addClickShortcut(Key.KEY_N,KeyModifier.ALT);

        this.adminView.addClickListener(e-> getUI().get().navigate("Admin"));

        this.settingsBtn.addClickListener(e-> getUI().get().navigate("Settings"));


        this.orderBox.addValueChangeListener(e->{
            if(isInit)
            refreshGridData();
        });

        this.categoryBox.addValueChangeListener(e->{
            if(e.getValue() instanceof Category){
                this.category= (Category) e.getValue();
            }else{
                this.category= null;
            }
            if(isInit)
            refreshGridData();
        });

        this.ascdescButton.addClickListener(e->{
           if(isASC){
               isASC=false;
               this.ascdescButton.setIcon(VaadinIcon.ARROW_DOWN.create());
           }else{
               isASC=true;
               this.ascdescButton.setIcon(VaadinIcon.ARROW_UP.create());
           }
           refreshGridData();
        });
    }

    @Override
    public void setInitValues(){
        orderBox.setValue(ColumnGrid.Order.Category);
        categoryBox.setValue("Alle");
        isInit=true;
    }

    @Override
    public void buildLayout(TaskRepository taskRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.add(new Label("Username: "+CurrentUser.getRole().getName()));

        this.form = new TaskForm(taskRepository, categoryRepository, this);
        form.setVisible(false);

        laterGridToday = new ColumnGrid<Task>("Later", Task.class, Task.Priority.LATER, taskRepository, this,userRepository);
        nextNweekGridTomorrow = new ColumnGrid<Task>("Next " + CurrentUser.getRole().getNweeksValue() + " Weeks", Task.class, Task.Priority.NEXTNWEEK, taskRepository, this,userRepository);
        nextWeekGridThisWeek = new ColumnGrid<Task>("Next Week", Task.class, Task.Priority.NEXTWEEK, taskRepository, this,userRepository);
        currentWeekGridThisWeek = new ColumnGrid<Task>("Current Week", Task.class, Task.Priority.CURRENTWEEK, taskRepository, this,userRepository);
        todayGridThisWeek = new ColumnGrid<Task>("Today", Task.class, Task.Priority.TODAY, taskRepository, this,userRepository);
        nearlyDoneGridThisWeek = new ColumnGrid<Task>("Nearly Done", Task.class, Task.Priority.NEARLYDONE, taskRepository, this,userRepository);
        donenGridThisWeek = new ColumnGrid<Task>("Done", Task.class, Task.Priority.DONE, taskRepository, this,userRepository);


        creatTermin.setIcon(VaadinIcon.PLUS.create());
        creatTermin.setClassName("btnLayout");

        settingsBtn.setIcon(VaadinIcon.COG.create());
        settingsBtn.setClassName("btnLayout");

        this.input.setWidth("130px");

        List<Object> list = new ArrayList<>();
        list.add("Alle");
        list.addAll(this.controller.getAllCategorysForUser());
        categoryBox.setItems(list);
        categoryBox.setAllowCustomValue(false);
        categoryBox.setItemLabelGenerator((ItemLabelGenerator) o -> {
            if (o instanceof Category){
                return ((Category) o).getBeschreibung();
            }else if(o instanceof String){
                return (String) o;
            }
            return "";
        });

        ascdescButton.setIcon(VaadinIcon.ARROW_UP.create());
        ascdescButton.getStyle().set("margin-left","0px");

        categoryBox.setWidth("170px");
        doneShow.setValue(true);
        orderBox.setItems(ColumnGrid.Order.values());
        orderBox.setAllowCustomValue(false);

        Button logoutButton = new Button("Logout",
                VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(event -> AccessControlFactory
                .getInstance().createAccessControl().signOut());

        HorizontalLayout headLayout = new HorizontalLayout();
        headLayout.addClassName("centerLayout");
        headLayout.getStyle().set("margin-top","0px");
        headLayout.add(settingsBtn,adminView,input,categoryBox,orderBox,ascdescButton,multiselectBox,doneShow,creatTermin,logoutButton);

        if(CurrentUser.getRole()!=null&&!CurrentUser.getRole().getRolle().equals(User.Rolle.ADMIN)){
            this.adminView.setVisible(false);
        }

        HorizontalLayout horizontalLayout = new HorizontalLayout(
                laterGridToday,
                nextNweekGridTomorrow,
                nextWeekGridThisWeek,
                currentWeekGridThisWeek,
                todayGridThisWeek,
                nearlyDoneGridThisWeek,
                donenGridThisWeek
        );
        horizontalLayout.setSizeFull();

        this.add(headLayout,horizontalLayout,form);
        this.setSizeFull();
        this.setClassName("main-layout");
    }

    @Override
    public void refreshGridData(){
        for (ColumnGrid<Task> grid :gridList){
            grid.refreshContainer(this.category,this.input.getValue(), this.orderBox.getValue(),this.isASC);
        }
    }

    @Override
    public void setVisibleForm(Task task){
        this.form.fillForm(task);
        this.form.setVisible(true);
    }

    @Override
    public void fillGridList() {
        gridList.add(todayGridThisWeek);
        gridList.add(nearlyDoneGridThisWeek);
        gridList.add(nextNweekGridTomorrow);
        gridList.add(nextWeekGridThisWeek);
        gridList.add(donenGridThisWeek);
        gridList.add(laterGridToday);
        gridList.add(currentWeekGridThisWeek);

        for (ColumnGrid<Task> grid :gridList){
            grid.setRowsDraggable(true);
            grid.setSizeFull();
            grid.setDropMode(GridDropMode.BETWEEN);
        }
    }
}
