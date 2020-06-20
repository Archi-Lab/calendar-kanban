package canban.component;

import authentication.CurrentUser;
import canban.entity.Task;
import canban.google.GoogleCalendarConnector;
import canban.repository.TaskRepository;
import canban.repository.UserRepository;
import canban.view.TaskView;
import canban.entity.Category;
import com.google.api.services.calendar.model.Event;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

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
                .setClassNameGenerator((ItemLabelGenerator<T>) t -> {
                    if(t instanceof Task){
                        return ((Task) t).getCategory().getBeschreibung();
                    }
                    return "";
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
        int height = CurrentUser.getUser().getPriorityHeightSettings().get(this.priority.name());
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
                retList=((List<T>) repository.findByColumnAndUserAndTitleContainingIgnoreCaseOrderByDoneDate(this.priority, CurrentUser.getUser(),string));
            }else {
                retList=((List<T>) repository.findByColumnAndUserOrderByDoneDate(this.priority, CurrentUser.getUser()));
            }
        }
        else{
            if(string.length()>1){
                retList=((List<T>) repository.findByColumnAndCategoryAndUserAndTitleContainingIgnoreCaseOrderByDoneDate(this.priority, category, CurrentUser.getUser(),string));
            }else {
                retList=((List<T>) repository.findByColumnAndCategoryAndUserOrderByDoneDate(this.priority, category, CurrentUser.getUser()));
            }
        }
        if(retList!=null) {
            if (priority != Task.Priority.DONE) {
                retList.sort((o1, o2) -> {
                    if(o1 instanceof Task && o2 instanceof Task) {
                        if(order.equals(Order.Category)){
                            if(asc)
                                return ((Task) o1).getCategory().getBeschreibung().compareTo(((Task) o2).getCategory().getBeschreibung());
                            else
                                return ((Task) o2).getCategory().getBeschreibung().compareTo(((Task) o1).getCategory().getBeschreibung());
                        }else if(order.equals(Order.Size)){
                            int o1int = CurrentUser.getUser().getSizeSettings().get(((Task) o1).getSize().toString());
                            int o2int = CurrentUser.getUser().getSizeSettings().get(((Task) o2).getSize().toString());
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
        int n = CurrentUser.getUser().getNweeksValue();
        LocalDate nowN = LocalDate.now().plusWeeks(2);
        TemporalField fieldISON = WeekFields.of(Locale.GERMANY).dayOfWeek();
        LocalDate startDateN = nowN.with(fieldISON,1);
        LocalDate endDateN = nowN.plusWeeks(n-1).with(fieldISON,7);
        int minutesNWeek =0;
        minutesNWeek=this.googleEventCheck(minutesNWeek,startDateN,endDateN);
        for(DayOfWeek day : DayOfWeek.values()){
            LocalTime timeday = CurrentUser.getUser().getTimeSettings().get(day.toString());
            LocalTime blcokedtimeday = CurrentUser.getUser().getBlockedTimeSettings().get(day.toString());
            maxSizeNWeek+= ((timeday.getHour()*60+timeday.getMinute())-(blcokedtimeday.getHour()*60+blcokedtimeday.getMinute()))*n;
        }
        if(maxSizeNWeek-minutesNWeek>0){
            maxSizeNWeek=(maxSizeNWeek-minutesNWeek)*(((double)(100-CurrentUser.getUser().getDistractionFactor()))/100);
        }else{
            maxSizeNWeek=(maxSizeNWeek-minutesNWeek);
        }
        this.checkBusy(retList,maxSizeNWeek);
    }

    private void checkDoneForNextWeek(List<T> retList) {
        double maxSizeWeek = 0;
        LocalDate now = LocalDate.now();
        TemporalField fieldISO = WeekFields.of(Locale.GERMANY).dayOfWeek();
        LocalDate startDateN = now.plusWeeks(1).with(fieldISO,1);
        LocalDate endDate = now.plusWeeks(1).with(fieldISO,7);
        int minutesNWeek =0;
        minutesNWeek=this.googleEventCheck(minutesNWeek,startDateN,endDate);
        for(DayOfWeek day : DayOfWeek.values()){
            LocalTime timeday = CurrentUser.getUser().getTimeSettings().get(day.toString());
            LocalTime blcokedtimeday = CurrentUser.getUser().getBlockedTimeSettings().get(day.toString());
            maxSizeWeek+= (timeday.getHour()*60+timeday.getMinute())-(blcokedtimeday.getHour()*60+blcokedtimeday.getMinute());
        }
        if(maxSizeWeek-minutesNWeek>0){
            maxSizeWeek=(maxSizeWeek-minutesNWeek)*(((double)(100-CurrentUser.getUser().getDistractionFactor()))/100);
        }else{
            maxSizeWeek=(maxSizeWeek-minutesNWeek);
        }
        this.checkBusy(retList,maxSizeWeek);

    }

    private int googleEventCheck(int minutes, LocalDate startDateN, LocalDate endDate) {
        if(CurrentUser.getUser().isConnectGoogle()){
            try {
                List<Event> eventList = GoogleCalendarConnector.connect(this.userRepository);
                if(eventList!=null&&!eventList.isEmpty()){
                    for (Event event:eventList){
                        String dateTime = event.getStart().get("dateTime").toString();
                        String[] split =dateTime.split("T");
                        if(split.length==2){
                            String dateEvent = split[0];
                            String timeEvent = split[1];
                            LocalTime start = LocalTime.parse(timeEvent.split("\\+")[0]);
                            LocalDate localDate = LocalDate.parse(dateEvent);
                            if((endDate==null&&startDateN==null&&localDate.isEqual(LocalDate.now()))||(localDate.isAfter(startDateN.minusDays(1))&&localDate.isBefore(endDate.plusDays(1)))){
                                String dateTimeEnd = event.getEnd().get("dateTime").toString();
                                String[] splitEnd =dateTimeEnd.split("T");
                                if(splitEnd.length==2){
                                    String timeEventEnd = splitEnd[1];
                                    LocalTime end = LocalTime.parse(timeEventEnd.split("\\+")[0]);
                                    minutes+=(int)start.until(end,ChronoUnit.MINUTES);
                                }
                            }
                        }
                    }
                }
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
        return minutes;
    }

    private void checkDoneForCurrentWeek(List<T> retList) {
        double maxSizeCurrentWeek = 0;
        LocalDate CurrentWeekNow = LocalDate.now();
        TemporalField fieldISOCurrentWeek = WeekFields.of(Locale.GERMANY).dayOfWeek();
        LocalDate endDateCurrentWeek = CurrentWeekNow.with(fieldISOCurrentWeek, 7);
        int minutesCurrentWeek = 0;
        minutesCurrentWeek = this.googleEventCheck(minutesCurrentWeek, CurrentWeekNow, endDateCurrentWeek);

        LocalTime timedayCurrentWeek;
        LocalTime timedayBlockedCurrentWeek;
        switch (CurrentWeekNow.getDayOfWeek()) {
            case MONDAY:
                timedayCurrentWeek = CurrentUser.getUser().getTimeSettings().get(DayOfWeek.MONDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getUser().getBlockedTimeSettings().get(DayOfWeek.MONDAY.toString());
                maxSizeCurrentWeek += (timedayCurrentWeek.getHour() * 60 + timedayCurrentWeek.getMinute()) - (timedayBlockedCurrentWeek.getHour() * 60 + timedayBlockedCurrentWeek.getMinute());
            case TUESDAY:
                timedayCurrentWeek = CurrentUser.getUser().getTimeSettings().get(DayOfWeek.TUESDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getUser().getBlockedTimeSettings().get(DayOfWeek.TUESDAY.toString());
                maxSizeCurrentWeek += (timedayCurrentWeek.getHour() * 60 + timedayCurrentWeek.getMinute()) - (timedayBlockedCurrentWeek.getHour() * 60 + timedayBlockedCurrentWeek.getMinute());
            case WEDNESDAY:
                timedayCurrentWeek = CurrentUser.getUser().getTimeSettings().get(DayOfWeek.WEDNESDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getUser().getBlockedTimeSettings().get(DayOfWeek.WEDNESDAY.toString());
                maxSizeCurrentWeek += (timedayCurrentWeek.getHour() * 60 + timedayCurrentWeek.getMinute()) - (timedayBlockedCurrentWeek.getHour() * 60 + timedayBlockedCurrentWeek.getMinute());
            case THURSDAY:
                timedayCurrentWeek = CurrentUser.getUser().getTimeSettings().get(DayOfWeek.THURSDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getUser().getBlockedTimeSettings().get(DayOfWeek.THURSDAY.toString());
                maxSizeCurrentWeek += (timedayCurrentWeek.getHour() * 60 + timedayCurrentWeek.getMinute()) - (timedayBlockedCurrentWeek.getHour() * 60 + timedayBlockedCurrentWeek.getMinute());
            case FRIDAY:
                timedayCurrentWeek = CurrentUser.getUser().getTimeSettings().get(DayOfWeek.FRIDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getUser().getBlockedTimeSettings().get(DayOfWeek.FRIDAY.toString());
                maxSizeCurrentWeek += (timedayCurrentWeek.getHour() * 60 + timedayCurrentWeek.getMinute()) - (timedayBlockedCurrentWeek.getHour() * 60 + timedayBlockedCurrentWeek.getMinute());
            case SATURDAY:
                timedayCurrentWeek = CurrentUser.getUser().getTimeSettings().get(DayOfWeek.SATURDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getUser().getBlockedTimeSettings().get(DayOfWeek.SATURDAY.toString());
                maxSizeCurrentWeek += (timedayCurrentWeek.getHour() * 60 + timedayCurrentWeek.getMinute()) - (timedayBlockedCurrentWeek.getHour() * 60 + timedayBlockedCurrentWeek.getMinute());
            case SUNDAY:
                timedayCurrentWeek = CurrentUser.getUser().getTimeSettings().get(DayOfWeek.SUNDAY.toString());
                timedayBlockedCurrentWeek = CurrentUser.getUser().getBlockedTimeSettings().get(DayOfWeek.SUNDAY.toString());
                maxSizeCurrentWeek += (timedayCurrentWeek.getHour() * 60 + timedayCurrentWeek.getMinute()) - (timedayBlockedCurrentWeek.getHour() * 60 + timedayBlockedCurrentWeek.getMinute());
        }
        if (maxSizeCurrentWeek - minutesCurrentWeek > 0){
            maxSizeCurrentWeek = (maxSizeCurrentWeek - minutesCurrentWeek) * (((double) (100 - CurrentUser.getUser().getDistractionFactor())) / 100);
        }else{
            maxSizeCurrentWeek = (maxSizeCurrentWeek - minutesCurrentWeek);
        }
        this.checkBusy(retList,maxSizeCurrentWeek);
    }

    private void checkBusy(List<T> retList, double maxSizeCurrentWeek) {
        int sizeOfTasksCurrentWeek= 0;
        for (T t:retList){
            if(t instanceof Task){
                int size = CurrentUser.getUser().getSizeSettings().get(((Task) t).getSize().toString());
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
        LocalTime time = CurrentUser.getUser().getTimeSettings().get(LocalDate.now().getDayOfWeek().toString());
        LocalTime blockedTime = CurrentUser.getUser().getBlockedTimeSettings().get(LocalDate.now().getDayOfWeek().toString());

        int minutes = 0;
        double maxsize;
        minutes=this.googleEventCheck(minutes,null,null);
        if(( (time.getHour()*60+time.getMinute()-minutes-blockedTime.getHour()*60-blockedTime.getMinute()))>0) {
             maxsize = (time.getHour() * 60 + time.getMinute() - minutes - blockedTime.getHour() * 60 - blockedTime.getMinute()) * (((double) (100 - CurrentUser.getUser().getDistractionFactor())) / 100);
        }else{
            maxsize = (time.getHour() * 60 + time.getMinute() - minutes - blockedTime.getHour() * 60 - blockedTime.getMinute());
        }
            this.checkBusy(retList,maxsize);
    }

    public void setVisibleItems(boolean show,Category category,String string,Order order,boolean asc){
        if(show){
            refreshContainer(category,string,order,asc);
        }else{
            this.setItems(Collections.emptyList());
        }
    }

    public enum Order{
        Category, Size, Deadline
    }
}
