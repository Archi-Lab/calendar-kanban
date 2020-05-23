package xmlexport;


import com.example.crudwithvaadin.entity.Category;
import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.TaskList;
import com.example.crudwithvaadin.entity.User;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbExample
{
    public static void main(String[] args)
    {

        Category bachelor = new Category("Bachelor");
        Category freizeit = new Category("Freizeit");

        User user = new User("test","test", User.Rolle.ADMIN);

        //Java object. We will convert it to XML.
        Task task1 = new Task();
        task1.setTitle("100 erledigen");
        task1.setCreationDate(LocalDate.now());
        task1.setColumn(Task.Priority.TODAY);
        task1.setCategory(bachelor);
        task1.setUser(user);

        Task task2 = new Task();
        task2.setTitle("101 erledigen");
        task2.setCreationDate(LocalDate.now());
        task2.setColumn(Task.Priority.LATER);
        task2.setCategory(freizeit);
        task2.setUser(user);
        task2.setUser(null);


        List<Task> taskList = new ArrayList<>();
        taskList.add(task1);
        taskList.add(task2);


        //Method which uses JAXB to convert object to XML
        File file = new File("employee.xml");
        try {
            marshal(taskList,file);
            for (Task task : unmarshal(file)){
                System.out.println(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void marshal(List<Task> tasks,File selectedFile) throws IOException, JAXBException {
        JAXBContext context;
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(selectedFile));
        context = JAXBContext.newInstance(TaskList.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
        m.marshal(new TaskList(tasks),writer);
        writer.close();
    }

    public static List<Task> unmarshal(File importFile) throws JAXBException{
        TaskList taskList = new TaskList();

        JAXBContext context = JAXBContext.newInstance(TaskList.class);
        Unmarshaller um = context.createUnmarshaller();
        taskList = (TaskList) um.unmarshal(importFile);

        return taskList.getList();
    }
}
