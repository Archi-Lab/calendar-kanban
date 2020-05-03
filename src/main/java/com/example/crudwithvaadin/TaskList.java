package com.example.crudwithvaadin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="tasks")
public class TaskList {

    @XmlElement(name ="task", type = Task.class)
    private List<Task> list = new ArrayList<>();

    public TaskList(List<Task> tasks) {
        list=tasks;
    }

    public TaskList() {
    }

    public List<Task> getList() {
        return list;
    }

    public void setList(List<Task> list) {
        this.list = list;
    }
}
