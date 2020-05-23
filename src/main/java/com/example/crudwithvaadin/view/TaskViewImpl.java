package com.example.crudwithvaadin.view;

import authentication.AccessControlFactory;
import authentication.CurrentUser;
import com.example.crudwithvaadin.component.ColumnGrid;
import com.example.crudwithvaadin.controller.TaskViewController;
import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.entity.User;
import com.example.crudwithvaadin.form.TaskForm;
import com.example.crudwithvaadin.repository.BlockedTaskRepository;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ItemLabelGenerator;
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
import com.example.crudwithvaadin.repository.CategoryRepository;
import com.example.crudwithvaadin.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CssImport("./styles/style.css")
@HtmlImport(value = "./html/file.html")
@Route("Termin")
public class TaskViewImpl extends VerticalLayout implements TaskView {

    //Components
    private TextField input = new TextField("Search");
    private ComboBox categoryBox = new ComboBox("Category");
    private ComboBox orderBox = new ComboBox("Sort");
    private Button creatTermin = new Button("Task");
    private Button settingsBtn = new Button();
    private Button adminView = new Button("Adminmask");
    private Checkbox multiselectBox = new Checkbox("Multiselect");
    private Checkbox doneShow = new Checkbox("Show Done");
    private ColumnGrid<Task> laterGridToday;
    private ColumnGrid<Task> nextNweekGridTomorrow;
    private ColumnGrid<Task> nextWeekGridThisWeek;
    private ColumnGrid<Task> currentWeekGridThisWeek;
    private ColumnGrid<Task> todayGridThisWeek;
    private ColumnGrid<Task> nearlyDoneGridThisWeek;
    private ColumnGrid<Task> donenGridThisWeek;

    //Controller
    private TaskViewController controller = new TaskViewController(this);

    //Forms
    private TaskForm form;

    //Repositorys
    private TaskRepository repo;
    private CategoryRepository categoryRepository;
    private BlockedTaskRepository blockedTaskRepository;

    //Other Variables
    private ColumnGrid<Task> dragSource = null;
    private List<Task> draggedItems = null;
    private Category category = null;
    private List<ColumnGrid<Task>> gridList = new ArrayList<>();

    public TaskViewImpl(TaskRepository repo, CategoryRepository categoryRepository, BlockedTaskRepository blockedTaskRepository) {
        if (CurrentUser.getRole() != null){
            this.repo = repo;
            this.blockedTaskRepository=blockedTaskRepository;
            this.categoryRepository = categoryRepository;
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
            repo.saveAll(draggedItems);

            dragSource.refreshContainer(category,input.getValue(),(ColumnGrid.Order) this.orderBox.getValue());
            targetGrid.refreshContainer(category,input.getValue(),(ColumnGrid.Order) this.orderBox.getValue());
        };

        for (ColumnGrid<Task> grid :gridList){
            grid.addDragStartListener(dragStartListener);
            grid.addDragEndListener(dragEndListener);
            grid.addDropListener(dropListener);
            grid.addItemDoubleClickListener(e->{
                this.controller.taskClicked(e.getItem());
            });
        }
        this.multiselectBox.addValueChangeListener(e->{
           gridList.forEach(grid->{
               grid.setSelectionMode(e.getValue()?Grid.SelectionMode.MULTI: Grid.SelectionMode.SINGLE);
           });
        });

        this.doneShow.addValueChangeListener(e->{
            this.donenGridThisWeek.setVisibleItems(e.getValue(),this.category,this.input.getValue(),(ColumnGrid.Order) this.orderBox.getValue());
        });

        this.input.setValueChangeMode(ValueChangeMode.LAZY);
        this.input.addValueChangeListener(e->{
            refreshGridData();
        });

        this.creatTermin.addClickListener(e->{
            this.controller.taskClicked(null);
        });

        this.adminView.addClickListener(e->{
            getUI().get().navigate("Admin");
        });

        this.settingsBtn.addClickListener(e->{
            getUI().get().navigate("Settings");
        });


        this.orderBox.addValueChangeListener(e->{
            refreshGridData();
        });

        this.categoryBox.addValueChangeListener(e->{
            if(e.getValue() instanceof Category){
                this.category= (Category) e.getValue();
            }else{
                this.category= null;
            }
            refreshGridData();
        });
    }

    @Override
    public void setInitValues(){
        orderBox.setValue(ColumnGrid.Order.Kategorie);
        categoryBox.setValue("Alle");
    }

    @Override
    public void buildLayout() {
        this.add(new Label("Username: "+CurrentUser.getRole().getName()));

        this.form = new TaskForm(repo, categoryRepository, this);
        form.setVisible(false);

        laterGridToday = new ColumnGrid<Task>("Later", Task.class, Task.Priority.LATER, repo, this,blockedTaskRepository);
        nextNweekGridTomorrow = new ColumnGrid<Task>("Next " + CurrentUser.getRole().getNweeksValue() + " Weeks", Task.class, Task.Priority.NEXTNWEEK, repo, this,blockedTaskRepository);
        nextWeekGridThisWeek = new ColumnGrid<Task>("Next Week", Task.class, Task.Priority.NEXTWEEK, repo, this,blockedTaskRepository);
        currentWeekGridThisWeek = new ColumnGrid<Task>("Current Week", Task.class, Task.Priority.CURRENTWEEK, repo, this,blockedTaskRepository);
        todayGridThisWeek = new ColumnGrid<Task>("Today", Task.class, Task.Priority.TODAY, repo, this,blockedTaskRepository);
        nearlyDoneGridThisWeek = new ColumnGrid<Task>("Nearly Done", Task.class, Task.Priority.NEARLYDONE, repo, this,blockedTaskRepository);
        donenGridThisWeek = new ColumnGrid<Task>("Done", Task.class, Task.Priority.DONE, repo, this,blockedTaskRepository);


        creatTermin.setIcon(VaadinIcon.PLUS.create());
        creatTermin.setClassName("createTermin");

        settingsBtn.setIcon(VaadinIcon.COG.create());
        settingsBtn.setClassName("createTermin");

        this.input.setWidth("130px");

        List<Object> list = new ArrayList<>();
        list.add("Alle");
        list.addAll(categoryRepository.findAll());
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
        headLayout.add(settingsBtn,adminView,input,categoryBox,orderBox,multiselectBox,doneShow,creatTermin,logoutButton);

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
            grid.refreshContainer(this.category,this.input.getValue(), (ColumnGrid.Order) this.orderBox.getValue());
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
