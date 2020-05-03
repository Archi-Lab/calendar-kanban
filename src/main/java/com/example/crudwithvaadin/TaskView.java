package com.example.crudwithvaadin;

import authentication.AccessControlFactory;
import authentication.CurrentUser;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CssImport("./styles/style.css")
@HtmlImport(value = "./html/file.html")
@Route("Termin")
public class TaskView extends VerticalLayout {

    private TextField input = new TextField("Suche");
    private ComboBox categoryBox = new ComboBox("Kategorie");
    private ComboBox orderBox = new ComboBox("Sortieren");
    private Button creatTermin = new Button();
    private Button settingsBtn = new Button();
    private Button adminView = new Button("Adminmaske");
    private Button createDump = new Button("Export");
    private Button deleteOldTask = new Button("Alte Task löschen");
    private Checkbox multiselectBox = new Checkbox("Mehrfachauswahl");
    private Checkbox doneShow = new Checkbox("Erledigte anzeigen");
    private List<ColumnGrid<Task>> gridList = new ArrayList<>();
    private ColumnGrid<Task> laterGridToday;
    private ColumnGrid<Task> nextNweekGridTomorrow;
    private ColumnGrid<Task> nextWeekGridThisWeek;
    private ColumnGrid<Task> currentWeekGridThisWeek;
    private ColumnGrid<Task> todayGridThisWeek;
    private ColumnGrid<Task> nearlyDoneGridThisWeek;
    private ColumnGrid<Task> donenGridThisWeek;
    private TaskForm form;
    private ColumnGrid<Task> dragSource = null;
    private List<Task> draggedItems = null;
    private Category category = null;

    private final TaskRepository repo;
    private final CategoryRepository categoryRepository;

    public TaskView(TaskRepository repo,CategoryRepository categoryRepository){
        this.repo=repo;
        this.categoryRepository=categoryRepository;
        this.form = new TaskForm(repo,categoryRepository,this);
        laterGridToday = new ColumnGrid<Task>("Later",Task.class, Task.Priority.LATER,repo,this);
        nextNweekGridTomorrow = new ColumnGrid<Task>("Next <n> Weeks",Task.class, Task.Priority.NEXTNWEEK,repo,this);
        nextWeekGridThisWeek = new ColumnGrid<Task>("Next Week",Task.class, Task.Priority.NEXTWEEK,repo,this);
        currentWeekGridThisWeek = new ColumnGrid<Task>("Current Week",Task.class, Task.Priority.CURRENTWEEK,repo,this);
        todayGridThisWeek = new ColumnGrid<Task>("Today",Task.class, Task.Priority.TODAY,repo,this);
        nearlyDoneGridThisWeek = new ColumnGrid<Task>("Nearly Done",Task.class, Task.Priority.NEARLYDONE,repo,this);
        donenGridThisWeek = new ColumnGrid<Task>("Done",Task.class, Task.Priority.DONE,repo,this);
        buildLayout();
        configListener();
        filltGridWithData(10);
    }

    private void configListener() {

        ComponentEventListener<GridDragStartEvent<Task>> dragStartListener = event -> {
            draggedItems = event.getDraggedItems();
            dragSource = (ColumnGrid<Task>) event.getSource();
            //todayGrid.setDropMode(GridDropMode.BETWEEN);
            //tomorrowGrid.setDropMode(GridDropMode.BETWEEN);
        };
        ComponentEventListener<GridDragEndEvent<Task>> dragEndListener = event -> {
            draggedItems = null;
            dragSource = null;
            //todayGrid.setDropMode(null);
            //tomorrowGrid.setDropMode(null);
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
                this.form.fillForm(e.getItem());
                this.form.setVisible(true);
            });
        }
        this.multiselectBox.addValueChangeListener(e->{
           gridList.forEach(grid->{grid.setSelectionMode(e.getValue()?Grid.SelectionMode.MULTI: Grid.SelectionMode.SINGLE);});
        });

        this.doneShow.addValueChangeListener(e->{
            this.donenGridThisWeek.setVisibleItems(e.getValue(),this.category,this.input.getValue(),(ColumnGrid.Order) this.orderBox.getValue());
        });


        this.input.setValueChangeMode(ValueChangeMode.LAZY);
        this.input.addValueChangeListener(e->{
            refresh();
        });

        this.creatTermin.addClickListener(e->{
            this.form.setVisible(true);
            this.form.fillForm(null);
        });

        this.adminView.addClickListener(e->{
            getUI().get().navigate("Admin");
        });

        this.settingsBtn.addClickListener(e->{
            getUI().get().navigate("Settings");
        });

        this.deleteOldTask.addClickListener(e->{
            this.repo.deleteAllByUserAndColumnAndDoneDateBefore(CurrentUser.getRole(), Task.Priority.DONE, LocalDate.now().minusMonths(4));
            this.donenGridThisWeek.refreshContainer(this.category,input.getValue(),(ColumnGrid.Order) this.orderBox.getValue());
        });
    }

    private void buildLayout() {

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

        creatTermin.setIcon(VaadinIcon.PLUS.create());
        creatTermin.setClassName("createTermin");

        settingsBtn.setIcon(VaadinIcon.COG.create());
        settingsBtn.setClassName("createTermin");

        List<Object> list = new ArrayList<>();
        list.add("Alle");
        list.addAll(categoryRepository.findAll());
        categoryBox.setItems(list);
        categoryBox.setAllowCustomValue(false);
        categoryBox.setItemLabelGenerator(new ItemLabelGenerator() {
            @Override
            public String apply(Object o) {
                if (o instanceof Category){
                    return ((Category) o).getBeschreibung();
                }else if(o instanceof String){
                    return (String) o;
                }
                return "";
            }
        });
        categoryBox.setValue("Alle");
        categoryBox.addValueChangeListener(e->{
            if(e.getValue() instanceof Category){
                this.category= (Category) e.getValue();
            }else{
                this.category= null;
            }
            refresh();
        });
        doneShow.setValue(true);
        orderBox.setItems(ColumnGrid.Order.values());
        orderBox.setAllowCustomValue(false);


        orderBox.addValueChangeListener(e->{
            refresh();
        });
        orderBox.setValue(ColumnGrid.Order.Alphabetisch);

        Button logoutButton = new Button("Logout",
                VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(event -> AccessControlFactory
                .getInstance().createAccessControl().signOut());
        HorizontalLayout headLayout = new HorizontalLayout();
        headLayout.add(creatTermin,settingsBtn,adminView,input,categoryBox,orderBox,multiselectBox,doneShow,createDump,deleteOldTask,logoutButton);

        if(CurrentUser.getRole()!=null&&!CurrentUser.getRole().getRolle().equals(User.Rolle.ADMIN)){
            this.adminView.setVisible(false);
        }

        HorizontalLayout horizontalLayout = new HorizontalLayout(
                nextNweekGridTomorrow,
                nextWeekGridThisWeek,
                currentWeekGridThisWeek,
                laterGridToday,
                todayGridThisWeek,
                nearlyDoneGridThisWeek,
                donenGridThisWeek
        );
        horizontalLayout.setSizeFull();
        add(headLayout,horizontalLayout,form);
        form.setVisible(false);
        this.setSizeFull();
    }
    private ListDataProvider<Task> createDataProvider() {
        List<Task> list = repo.findAll();
        return new ListDataProvider<>(list);
    }

    private void filltGridWithData(int anzahl) {
        //int i=0;
        //this.captionGridToday.setItems(repo.findAll());
        refresh();
    }

    public void refresh(){
        for (ColumnGrid<Task> grid :gridList){
            grid.refreshContainer(this.category,this.input.getValue(), (ColumnGrid.Order) this.orderBox.getValue());
        }
    }

    public void setVisibleForm(Task task){
        this.form.fillForm(task);
        this.form.setVisible(true);
    }

}
