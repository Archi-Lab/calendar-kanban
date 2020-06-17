package xmlexport;

import canban.entity.Category;
import canban.entity.Task;

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


    @XmlElement(name ="category", type = Category.class)
    private List<Category> listCategory = new ArrayList<>();

    public TaskList(List<Task> tasks) {
        list=tasks;
    }

    public TaskList() {
    }

    public TaskList(List<Task> list, List<Category> listCategory) {
        this.list = list;
        this.listCategory = listCategory;
    }

    public List<Category> getListCategory() {
        return listCategory;
    }

    public void setListCategory(List<Category> listCategory) {
        this.listCategory = listCategory;
    }

    public List<Task> getList() {
        return list;
    }

    public void setList(List<Task> list) {
        this.list = list;
    }
}
