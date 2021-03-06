package xmlexport;


import canban.entity.Category;
import canban.entity.Task;
import canban.view.AdminViewImpl;
import com.vaadin.flow.component.upload.receivers.FileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbConverter
{
    private static final Logger log = LoggerFactory.getLogger(JaxbConverter.class);

    public static String export(List<Task> taskList, List<Category> categoryList)
    {
        //Method which uses JAXB to convert object to XML
        File file = new File("export.xml");
        try {
            List<Task> taskList1 = new ArrayList<>();
            List<Category> categoryList1 = new ArrayList<>();
            for (Task task:taskList
                 ) {
                task.setUser(null);
                task.getCategory().setOwner(null);
                taskList1.add(task);
            }
            for (Category category:categoryList
                 ) {
                category.setOwner(null);
                categoryList1.add(category);
            }
            marshal(taskList1,categoryList1,file);
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static void marshal(List<Task> tasks, List<Category> categoryList, File selectedFile) throws IOException, JAXBException {
        JAXBContext context;
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(selectedFile));
        context = JAXBContext.newInstance(TaskList.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
        m.marshal(new TaskList(tasks,categoryList),writer);
        writer.close();
    }

    public static TaskList unmarshal(FileData importFile) throws JAXBException{
        TaskList taskList = new TaskList();
        String filename = "import"+UUID.randomUUID().toString()+".xml";
        ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) importFile.getOutputBuffer();
        try(OutputStream outputStream = new FileOutputStream(filename)) {
            byteArrayOutputStream.writeTo(outputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        File file = new File(filename);
        JAXBContext context = JAXBContext.newInstance(TaskList.class);
        Unmarshaller um = context.createUnmarshaller();
        taskList = (TaskList) um.unmarshal(file);
        file.delete();
        return taskList;
    }
}
