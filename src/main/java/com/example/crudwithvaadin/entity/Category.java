package com.example.crudwithvaadin.entity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@XmlRootElement
public class Category {


    @Id
    @GeneratedValue
    private Long id;

    private String beschreibung;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<Task> taskSet = new HashSet<>();

    @ManyToOne
    private User owner;

    private String color;

    public Category() {
    }

    public Category(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public void addTask(Task task){
        if(taskSet.contains(task)){return;}
        taskSet.add(task);
        task.setCategory(this);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
