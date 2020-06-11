package com.example.crudwithvaadin.component;

import authentication.CurrentUser;
import com.example.crudwithvaadin.google.GoogleCalendarConnector;
import com.example.crudwithvaadin.repository.UserRepository;
import com.example.crudwithvaadin.view.TaskView;
import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.google.api.services.calendar.model.Event;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.example.crudwithvaadin.repository.TaskRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@HtmlImport(value = "./html/file.html")
public class ColumnGrid<T> extends Grid<T> {

    private final TaskView taskView;
    private Task.Priority priority;
    private TaskRepository repository;
    private UserRepository userRepository;
    private String rootCaption;

    public ColumnGrid(String caption, Class<T> beanType, Task.Priority priority, TaskRepository repository, TaskView taskView,UserRepository userRepository){
        super(beanType,false);
        this.addComponentColumn(this::generateLabel)
                .setHeader(caption)
                .setKey(caption)
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
        this.userRepository=userRepository;
        this.rootCaption=caption;
        this.repository=repository;
        this.priority=priority;
        this.taskView=taskView;
    }

    private HorizontalLayout generateLabel(T t) {
        HorizontalLayout retLayout = new HorizontalLayout();
        int height = CurrentUser.getRole().getPriorityHeightSettings().get(this.priority.name());
        retLayout.getStyle().set("height",height+"px");
        retLayout.getStyle().set("text-align","center");
        retLayout.getStyle().set("vertical-align","center");
        retLayout.getStyle().set("line-height",height+"px");
        Label retLabel = new Label();
        if(t instanceof Task){
            String value="";
            if(((Task) t).getDueDate()!=null){
                if(((Task) t).getDueDate().isBefore(LocalDate.now())){
                    retLayout.add(VaadinIcon.WARNING.create());
                    retLabel.getStyle().set("margin-left","0px");
                }else if(((Task) t).getDueDate().minusDays(5).isBefore(LocalDate.now())){
                    retLayout.add(VaadinIcon.BELL.create());
                    retLabel.getStyle().set("margin-left","0px");
                }
            }
            if(((Task) t).getTitle().length()>15){
                value+=(((Task) t).getTitle().substring(0,14)+"...");
            }else {
                value+=(((Task) t).getTitle());
            }
            retLabel.setText(value);
         retLayout.getStyle().set("background",((Task) t).getCategory().getColor());
         long colorNr = Integer.parseInt(((Task) t).getCategory().getColor().substring(1),16);
         if(colorNr>(16777215/2)){
             retLayout.getStyle().set("color","white");
         }
         switch (((Task) t).getSize()){
             case S:
                 retLayout.getStyle().set("border-left","6px solid chartreuse");
                 break;
             case M:
                 retLayout.getStyle().set("border-left","6px solid darkgrey");
                 break;
             case L:
                 retLayout.getStyle().set("border-left","6px solid orchid");
                 break;
             case XL:
                 retLayout.getStyle().set("border-left","6px solid coral");
                 break;
         }
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

    public void refreshContainer(Category category, String string, Order order, boolean asc){
        List<T> retList=null;
        if(category==null) {
            if(string.length()>1){
                retList=((List<T>) repository.findByColumnAndUserAndTitleContainingIgnoreCaseOrderByDoneDate(this.priority, CurrentUser.getRole(),string));
            }else {
                retList=((List<T>) repository.findByColumnAndUserOrderByDoneDate(this.priority, CurrentUser.getRole()));
            }
        }
        else if (category!=null) {
            if(string.length()>1){
                retList=((List<T>) repository.findByColumnAndCategoryAndUserAndTitleContainingIgnoreCaseOrderByDoneDate(this.priority, category, CurrentUser.getRole(),string));
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
                            if(order.equals(Order.Category)){
                                if(asc)
                                    return ((Task) o1).getCategory().getBeschreibung().compareTo(((Task) o2).getCategory().getBeschreibung());
                                else
                                    return ((Task) o2).getCategory().getBeschreibung().compareTo(((Task) o1).getCategory().getBeschreibung());
                            }else if(order.equals(Order.Size)){
                                int o1int = CurrentUser.getRole().getSizeSettings().get(((Task) o1).getSize().toString());
                                int o2int = CurrentUser.getRole().getSizeSettings().get(((Task) o2).getSize().toString());
                                if(asc)
                                    return o1int-o2int;
                                else
                                    return o2int-o1int;
                            }else if(order.equals(Order.Deadline)){
                                if(((Task) o2).getDueDate()==null && ((Task) o1).getDueDate()==null) return 0;
                                if(((Task) o2).getDueDate()==null) return -1;
                                if(((Task) o1).getDueDate()==null) return 1;
                                Long tage1 = ChronoUnit.DAYS.between(LocalDate.now(), ((Task) o1).getDueDate().atStartOfDay());
                                Long tage2 = ChronoUnit.DAYS.between(LocalDate.now(), ((Task) o2).getDueDate().atStartOfDay());
                                if(asc)
                                    return tage1.compareTo(tage2);
                                else
                                    return tage2.compareTo(tage1);
                            }
                        }
                        return 0;
                    }
                });
            }
            this.setItems(retList);
            this.checkDoneFeasibility(retList);
        }
    }

    private void checkDoneFeasibility(List<T> retList) {
        switch(priority) {
            case TODAY:
                this.checkDoneForToday(retList);
                break;
            case CURRENTWEEK:
                this.checkDoneForCurrentWeek(retList);
                break;
            case NEXTWEEK:
                this.checkDoneForNextWeek(retList);
                break;
            case NEXTNWEEK:
                this.checkDoneForNextNWeek(retList);
                break;
        }
    }

    private void checkDoneForNextNWeek(List<T> retList) {
        double maxSizeNWeek = 0;
        int n = CurrentUser.getRole().getNweeksValue();
        LocalDate nowN = LocalDate.now().plusWeeks(2);
        TemporalField fieldISON = WeekFields.of(Locale.GERMANY).dayOfWeek();
        LocalDate startDateN = nowN.with(fieldISON,1);
        LocalDate endDateN = nowN.plusWeeks(n-1).with(fieldISON,7);
        int minutesNWeek =0;
        if(CurrentUser.getRole().isConnectGoogle()){
            try {
                List<Event> eventList = GoogleCalendarConnector.connect(this.userRepository);
                if(eventList!=null&&!eventList.isEmpty()){
                    for (Event event:eventList){
                        String dateTime = event.getStart().get("dateTime").toString();
                        String split[]=dateTime.split("T");
                        if(split.length==2){
                            String dateEvent = split[0];
                            String timeEvent = split[1];
                            LocalTime start = LocalTime.parse(timeEvent.split("\\+")[0]);
                            LocalDate localDate = LocalDate.parse(dateEvent);
                            if(localDate.isAfter(startDateN.minusDays(1))&&localDate.isBefore(endDateN.plusDays(1))){
                                String dateTimeEnd = event.getEnd().get("dateTime").toString();
                                String splitEnd[]=dateTimeEnd.split("T");
                                if(splitEnd.length==2){
                                    String timeEventEnd = splitEnd[1];
                                    LocalTime end = LocalTime.parse(timeEventEnd.split("\\+")[0]);
                                    minutesNWeek+=(int)start.until(end,ChronoUnit.MINUTES);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }


        for(DayOfWeek day : DayOfWeek.values()){
            LocalTime timeday = CurrentUser.getRole().getTimeSettings().get(day.toString());
            LocalTime blcokedtimeday = CurrentUser.getRole().getBlockedTimeSettings().get(day.toString());
            maxSizeNWeek+= ((timeday.getHour()*60+timeday.getMinute())-(blcokedtimeday.getHour()*60+blcokedtimeday.getMinute()))*n;
        }
        maxSizeNWeek=(maxSizeNWeek-minutesNWeek)*(((double)(100-CurrentUser.getRole().getDistractionFactor()))/100);

        int sizeOfTasksNWeek= 0;
        for (T t:retList){
            if(t instanceof Task){
                int size = CurrentUser.getRole().getSizeSettings().get(((Task) t).getSize().toString());
                sizeOfTasksNWeek+=size;
            }
        }
        if(maxSizeNWeek<sizeOfTasksNWeek){
            this.addClassName("toBusyGrid");
            this.getColumnByKey(rootCaption).setHeader(rootCaption+" ("+Math.round(maxSizeNWeek-sizeOfTasksNWeek)+" Min)");
        }else{
            this.getColumnByKey(rootCaption).setHeader(rootCaption);
            this.removeClassName("toBusyGrid");
        }

    }

    private void checkDoneForNextWeek(List<T> retList) {
        double maxSizeWeek = 0;
        LocalDate now = LocalDate.now();
        TemporalField fieldISO = WeekFields.of(Locale.GERMANY).dayOfWeek();
        LocalDate startDate = now.plusWeeks(1).with(fieldISO,1);
        LocalDate endDate = now.plusWeeks(1).with(fieldISO,7);
        int minutesWeek =0;

        if(CurrentUser.getRole().isConnectGoogle()){
            try {
                List<Event> eventList = GoogleCalendarConnector.connect(this.userRepository);
                if(eventList!=null&&!eventList.isEmpty()){
                    for (Event event:eventList){
                        String dateTime = event.getStart().get("dateTime").toString();
                        String split[]=dateTime.split("T");
                        if(split.length==2){
                            String dateEvent = split[0];
                            String timeEvent = split[1];
                            LocalTime start = LocalTime.parse(timeEvent.split("\\+")[0]);
                            LocalDate localDate = LocalDate.parse(dateEvent);
                            if(localDate.isAfter(startDate.minusDays(1))&&localDate.isBefore(endDate.plusDays(1))){
                                String dateTimeEnd = event.getEnd().get("dateTime").toString();
                                String splitEnd[]=dateTimeEnd.split("T");
                                if(splitEnd.length==2){
                                    String timeEventEnd = splitEnd[1];
                                    LocalTime end = LocalTime.parse(timeEventEnd.split("\\+")[0]);
                                    minutesWeek+=(int)start.until(end,ChronoUnit.MINUTES);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }

        for(DayOfWeek day : DayOfWeek.values()){
            LocalTime timeday = CurrentUser.getRole().getTimeSettings().get(day.toString());
            LocalTime blcokedtimeday = CurrentUser.getRole().getBlockedTimeSettings().get(day.toString());
            maxSizeWeek+= (timeday.getHour()*60+timeday.getMinute())-(blcokedtimeday.getHour()*60+blcokedtimeday.getMinute());
        }
        maxSizeWeek=(maxSizeWeek-minutesWeek)*(((double)(100-CurrentUser.getRole().getDistractionFactor()))/100);

        int sizeOfTasksweek= 0;
        for (T t:retList){
            if(t instanceof Task){
                int size = CurrentUser.getRole().getSizeSettings().get(((Task) t).getSize().toString());
                sizeOfTasksweek+=size;
            }
        }
        if(maxSizeWeek<sizeOfTasksweek){
            this.addClassName("toBusyGrid");
            this.getColumnByKey(rootCaption).setHeader(rootCaption+" ("+Math.round(maxSizeWeek-sizeOfTasksweek)+" Min)");
        }else{
            this.getColumnByKey(rootCaption).setHeader(rootCaption);
            this.removeClassName("toBusyGrid");
        }
    }

    private void checkDoneForCurrentWeek(List<T> retList) {
        double maxSizeCurrentWeek = 0;
        LocalDate CurrentWeekNow = LocalDate.now();
        TemporalField fieldISOCurrentWeek = WeekFields.of(Locale.GERMANY).dayOfWeek();
        LocalDate endDateCurrentWeek = CurrentWeekNow.with(fieldISOCurrentWeek,7);
        int minutesCurrentWeek =0;

        if(CurrentUser.getRole().isConnectGoogle()){
            try {
                List<Event> eventList = GoogleCalendarConnector.connect(this.userRepository);
                if(eventList!=null&&!eventList.isEmpty()){
                    for (Event event:eventList){
                        String dateTime = event.getStart().get("dateTime").toString();
                        String split[]=dateTime.split("T");
                        if(split.length==2){
                            String dateEvent = split[0];
                            String timeEvent = split[1];
                            LocalTime start = LocalTime.parse(timeEvent.split("\\+")[0]);
                            LocalDate localDate = LocalDate.parse(dateEvent);
                            if(localDate.isAfter(CurrentWeekNow.minusDays(1))&&localDate.isBefore(endDateCurrentWeek.plusDays(1))){
                                String dateTimeEnd = event.getEnd().get("dateTime").toString();
                                String splitEnd[]=dateTimeEnd.split("T");
                                if(splitEnd.length==2){
                                    String timeEventEnd = splitEnd[1];
                                    LocalTime end = LocalTime.parse(timeEventEnd.split("\\+")[0]);
                                    minutesCurrentWeek+=(int)start.until(end,ChronoUnit.MINUTES);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }




        LocalTime timedayCurrentWeek;
        LocalTime timedayBlockedCurrentWeek;
        switch (CurrentWeekNow.getDayOfWeek()){
            case MONDAY:
                timedayCurrentWeek = CurrentUser.getRole().getTimeSettings().get(DayOfWeek.MONDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getRole().getBlockedTimeSettings().get(DayOfWeek.MONDAY.toString());
                maxSizeCurrentWeek+= (timedayCurrentWeek.getHour()*60+timedayCurrentWeek.getMinute())-(timedayBlockedCurrentWeek.getHour()*60+timedayBlockedCurrentWeek.getMinute());
            case TUESDAY:
                timedayCurrentWeek = CurrentUser.getRole().getTimeSettings().get(DayOfWeek.TUESDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getRole().getBlockedTimeSettings().get(DayOfWeek.TUESDAY.toString());
                maxSizeCurrentWeek+= (timedayCurrentWeek.getHour()*60+timedayCurrentWeek.getMinute())-(timedayBlockedCurrentWeek.getHour()*60+timedayBlockedCurrentWeek.getMinute());
            case WEDNESDAY:
                timedayCurrentWeek = CurrentUser.getRole().getTimeSettings().get(DayOfWeek.WEDNESDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getRole().getBlockedTimeSettings().get(DayOfWeek.WEDNESDAY.toString());
                maxSizeCurrentWeek+= (timedayCurrentWeek.getHour()*60+timedayCurrentWeek.getMinute())-(timedayBlockedCurrentWeek.getHour()*60+timedayBlockedCurrentWeek.getMinute());
            case THURSDAY:
                timedayCurrentWeek = CurrentUser.getRole().getTimeSettings().get(DayOfWeek.THURSDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getRole().getBlockedTimeSettings().get(DayOfWeek.THURSDAY.toString());
                maxSizeCurrentWeek+= (timedayCurrentWeek.getHour()*60+timedayCurrentWeek.getMinute())-(timedayBlockedCurrentWeek.getHour()*60+timedayBlockedCurrentWeek.getMinute());
            case FRIDAY:
                timedayCurrentWeek = CurrentUser.getRole().getTimeSettings().get(DayOfWeek.FRIDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getRole().getBlockedTimeSettings().get(DayOfWeek.FRIDAY.toString());
                maxSizeCurrentWeek+= (timedayCurrentWeek.getHour()*60+timedayCurrentWeek.getMinute())-(timedayBlockedCurrentWeek.getHour()*60+timedayBlockedCurrentWeek.getMinute());
            case SATURDAY:
                timedayCurrentWeek = CurrentUser.getRole().getTimeSettings().get(DayOfWeek.SATURDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getRole().getBlockedTimeSettings().get(DayOfWeek.SATURDAY.toString());
                maxSizeCurrentWeek+= (timedayCurrentWeek.getHour()*60+timedayCurrentWeek.getMinute())-(timedayBlockedCurrentWeek.getHour()*60+timedayBlockedCurrentWeek.getMinute());
            case SUNDAY:
                timedayCurrentWeek = CurrentUser.getRole().getTimeSettings().get(DayOfWeek.SUNDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getRole().getBlockedTimeSettings().get(DayOfWeek.SUNDAY.toString());
                maxSizeCurrentWeek+= (timedayCurrentWeek.getHour()*60+timedayCurrentWeek.getMinute())-(timedayBlockedCurrentWeek.getHour()*60+timedayBlockedCurrentWeek.getMinute());
        }

        maxSizeCurrentWeek=(maxSizeCurrentWeek-minutesCurrentWeek)*(((double)(100-CurrentUser.getRole().getDistractionFactor()))/100);

        int sizeOfTasksCurrentWeek= 0;
        for (T t:retList){
            if(t instanceof Task){
                int size = CurrentUser.getRole().getSizeSettings().get(((Task) t).getSize().toString());
                sizeOfTasksCurrentWeek+=size;
            }
        }
        if(maxSizeCurrentWeek<sizeOfTasksCurrentWeek){
            this.addClassName("toBusyGrid");
            this.getColumnByKey(rootCaption).setHeader(rootCaption+" ("+Math.round(maxSizeCurrentWeek-sizeOfTasksCurrentWeek)+" Min)");
        }else{
            this.getColumnByKey(rootCaption).setHeader(rootCaption);
            this.removeClassName("toBusyGrid");
        }
    }

    private void checkDoneForToday(List<T> retList) {
        LocalTime time = CurrentUser.getRole().getTimeSettings().get(LocalDate.now().getDayOfWeek().toString());
        LocalTime blockedTime = CurrentUser.getRole().getBlockedTimeSettings().get(LocalDate.now().getDayOfWeek().toString());

        int minutes = 0;

        if(CurrentUser.getRole().isConnectGoogle()){
            try {
                List<Event> eventList = GoogleCalendarConnector.connect(this.userRepository);
                if(eventList!=null&&!eventList.isEmpty()){
                    for (Event event:eventList){
                        String dateTime = event.getStart().get("dateTime").toString();
                        String split[]=dateTime.split("T");
                        if(split.length==2){
                            String dateEvent = split[0];
                            String timeEvent = split[1];
                            LocalTime start = LocalTime.parse(timeEvent.split("\\+")[0]);
                            LocalDate localDate = LocalDate.parse(dateEvent);
                            if(localDate.isEqual(LocalDate.now())){
                                String dateTimeEnd = event.getEnd().get("dateTime").toString();
                                String splitEnd[]=dateTimeEnd.split("T");
                                if(splitEnd.length==2){
                                    String timeEventEnd = splitEnd[1];
                                    LocalTime end = LocalTime.parse(timeEventEnd.split("\\+")[0]);
                                    minutes+=(int)start.until(end,ChronoUnit.MINUTES);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }

        double maxsize = (time.getHour()*60+time.getMinute()-minutes-blockedTime.getHour()*60-blockedTime.getMinute())*(((double)(100-CurrentUser.getRole().getDistractionFactor()))/100);
        int sizeOfTasks= 0;
        for (T t:retList){
            if(t instanceof Task){
                int size = CurrentUser.getRole().getSizeSettings().get(((Task) t).getSize().toString());
                sizeOfTasks+=size;
            }
        }
        if(maxsize<sizeOfTasks){
            this.addClassName("toBusyGrid");
            this.getColumnByKey(rootCaption).setHeader(rootCaption+" ("+Math.round(maxsize-sizeOfTasks)+" Min)");
        }else{
            this.getColumnByKey(rootCaption).setHeader(rootCaption);
            this.removeClassName("toBusyGrid");
        }
    }

    public void setVisibleItems(boolean show,Category category,String string,Order order,boolean asc){
        if(show){
            refreshContainer(category,string,order,asc);
        }else{
            this.setItems(Arrays.asList());
        }
    }

    public enum Order{
        Category, Size, Deadline
    }
}
