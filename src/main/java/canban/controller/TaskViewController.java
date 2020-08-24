package canban.controller;

import authentication.CurrentUser;
import canban.entity.Task;
import canban.repository.TaskRepository;
import canban.repository.CategoryRepository;
import canban.repository.UserRepository;
import canban.view.TaskView;
import canban.view.TaskViewImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class TaskViewController {

    private static final Logger log = LoggerFactory.getLogger(TaskViewController.class);

    private UserRepository userRepository;
    private TaskView view;
    private TaskRepository taskRepository;
    private CategoryRepository categoryRepository;

    public TaskViewController(TaskViewImpl view, TaskRepository taskRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.view = view;
        this.taskRepository=taskRepository;
        this.categoryRepository=categoryRepository;
        this.userRepository=userRepository;
    }

    public void onEnter() {
        view.buildLayout(this.taskRepository,this.categoryRepository,this.userRepository);
        view.fillGridList();
        view.configListener();
        view.setInitValues();
        view.refreshGridData();
    }

    public void taskClicked(Task task){
        this.view.setVisibleForm(task);
    }

    public void saveTasks(List<Task> draggedItems) {
        taskRepository.saveAll(draggedItems);
    }

    public Collection<?> getAllCategorysForUser() {
        return categoryRepository.findByOwner(CurrentUser.getUser());
    }
}
