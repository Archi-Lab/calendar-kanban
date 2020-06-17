package canban.view;

import canban.entity.Task;
import canban.repository.CategoryRepository;
import canban.repository.TaskRepository;
import canban.repository.UserRepository;

public interface TaskView {
    void configListener();

    void setInitValues();

    void buildLayout(TaskRepository taskRepository, CategoryRepository categoryRepository, UserRepository userRepository);

    void refreshGridData();

    void setVisibleForm(Task task);

    void fillGridList();
}
