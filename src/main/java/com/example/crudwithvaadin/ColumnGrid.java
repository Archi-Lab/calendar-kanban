package com.example.crudwithvaadin;

import authentication.CurrentUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.function.SerializableFunction;
import org.aspectj.weaver.ast.Or;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@HtmlImport(value = "./html/file.html")
public class ColumnGrid<T> extends Grid<T> {

    private final TaskView taskView;
    private Task.Priority priority;
    private TaskRepository repository;

    public ColumnGrid(String caption,Class<T> beanType, Task.Priority priority, TaskRepository repository, TaskView taskView){
        super(beanType,false);
        Grid.Column<T> column = this.addComponentColumn(this::generateLabel)
                .setHeader(caption)
                .setAutoWidth(true)
                .setClassNameGenerator(new ItemLabelGenerator<T>() {
                    @Override
                    public String apply(T t) {
                        if(t instanceof Task){
                            return ((Task) t).getCategory().getBeschreibung();
                        }
                        return "";
                    }
                })
                .setSortable(false);
        this.getHeaderRows().removeAll(this.getHeaderRows());
        this.repository=repository;
        this.priority=priority;
        this.taskView=taskView;
    }

    private HorizontalLayout generateLabel(T t) {
        HorizontalLayout retLayout = new HorizontalLayout();
        Label retLabel = new Label();
        if(t instanceof Task){
         retLabel.setText(((Task) t).getBeschreibung());
         retLabel.setSizeFull();
         retLayout.getStyle().set("background",((Task) t).getCategory().getColor());
         retLayout.addClassName("columnitem");
         retLayout.add(retLabel);
         retLayout.addClickListener(e->{
             if(e.getClickCount()>=2){
                 this.taskView.setVisibleForm((Task)t);
             }
         });
        }
        return retLayout;
    }

    public Task.Priority getPriority() {
        return priority;
    }

    public void setPriority(Task.Priority priority) {
        this.priority = priority;
    }

    public void refreshContainer(Category category,String string, Order order){
        List<T> retList=null;
        if(category==null) {
            if(string.length()>1){
                retList=((List<T>) repository.findByColumnAndUserAndBeschreibungContainingIgnoreCaseOrderByDoneDate(this.priority, CurrentUser.getRole(),string));
            }else {
                retList=((List<T>) repository.findByColumnAndUserOrderByDoneDate(this.priority, CurrentUser.getRole()));
            }
        }
        else if (category!=null) {
            if(string.length()>1){
                retList=((List<T>) repository.findByColumnAndCategoryAndUserAndBeschreibungContainingIgnoreCaseOrderByDoneDate(this.priority, category, CurrentUser.getRole(),string));
            }else {
                retList=((List<T>) repository.findByColumnAndCategoryAndUserOrderByDoneDate(this.priority, category, CurrentUser.getRole()));
            }
        }
        if(retList!=null) {
            if (priority != Task.Priority.DONE) {
                retList.sort(new Comparator<T>() {
                    @Override
                    public int compare(T o1, T o2) {
                        if(o1 instanceof Task && o2 instanceof Task) {
                            if (order.equals(Order.Alphabetisch)) {
                                return ((Task) o1).getBeschreibung().compareTo(((Task) o2).getBeschreibung());
                            }else if(order.equals(Order.Kategorie)){
                                return ((Task) o1).getCategory().getBeschreibung().compareTo(((Task) o2).getCategory().getBeschreibung());
                            }else if(order.equals(Order.Größe)){
                                int o1int = CurrentUser.getRole().getSizeSettings().get(((Task) o1).getSize());
                                int o2int = CurrentUser.getRole().getSizeSettings().get(((Task) o2).getSize());
                                return o2int-o1int;
                            }else if(order.equals(Order.AnzahlDerTageBisEnde)){
                                if(((Task) o2).getDueDate()==null && ((Task) o1).getDueDate()==null) return 0;
                                if(((Task) o2).getDueDate()==null) return 1;
                                if(((Task) o1).getDueDate()==null) return 2;
                                Long tage1 = ChronoUnit.DAYS.between(LocalDate.now(), ((Task) o1).getDueDate().atStartOfDay());
                                Long tage2 = ChronoUnit.DAYS.between(LocalDate.now(), ((Task) o2).getDueDate().atStartOfDay());
                                return tage1.compareTo(tage2);
                            }
                        }
                        return 0;
                    }
                });
            }
            this.setItems(retList);
        }
    }

    public void setVisibleItems(boolean show,Category category,String string,Order order){
        if(show){
            refreshContainer(category,string,order);
        }else{
            this.setItems(Arrays.asList());
        }
    }

    public enum Order{
        Alphabetisch,Kategorie,Größe,AnzahlDerTageBisEnde
    }
}
