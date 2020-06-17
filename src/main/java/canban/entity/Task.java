package canban.entity;


import xmlexport.LocalDateAdapter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="task")
public class Task {

    @Id
    private UUID id = UUID.randomUUID();

    private String title;

    private boolean done;

    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate dueDate;

    //@NotNull
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate creationDate;

    private Priority column;

    @ManyToOne
    private Category category;

    private Size size;

    @ManyToOne
    private User user;

    public Task(String title, LocalDate dueDate){
        this.title = title;
        this.dueDate = dueDate;
    }

    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate doneDate;

    public Task(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String beschreibung) {
        this.title = beschreibung;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate datum) {
        this.dueDate = datum;
    }

    @Override
    public String toString() {
        return "Termin{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", done=" + done +
                ", datum=" + dueDate +
                '}';
    }

    public enum Priority{

        LATER("Later"),NEXTNWEEK("Next $ Week"),NEXTWEEK("Next Week"),
        CURRENTWEEK("Current Week"),TODAY("Today"),NEARLYDONE("Nearly Done"),DONE("Done");

        private String bezeichnung;

        Priority(String bezeichnung) {
            this.bezeichnung=bezeichnung;
        }

        public String getBezeichnung() {
            return bezeichnung;
        }
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Priority getColumn() {
        return column;
    }

    public void setColumn(Priority column) {
        this.column = column;
        if(this.column==Priority.DONE){
            this.doneDate=LocalDate.now();
        }else if(doneDate != null){
            doneDate=null;
        }
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (this.category!=null && this.category.equals(category))return;
        this.category = category;
        category.addTask(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDoneDate() {
        return doneDate;
    }

    public void setDoneDate(LocalDate doneDate) {
        this.doneDate = doneDate;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public enum Size{
        S,M,L,XL
    }

    public Task copy(){
        Task task = new Task();
        task.setColumn(this.getColumn());
        task.setCreationDate(this.getCreationDate());
        task.setDueDate(this.getDueDate());
        task.setSize(this.getSize());
        task.setTitle(this.getTitle());
        task.setDone(this.isDone());
        task.setDoneDate(this.getDoneDate());
        return task;
    }
}
