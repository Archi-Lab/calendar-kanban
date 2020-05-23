package com.example.crudwithvaadin.view;

import com.example.crudwithvaadin.entity.Task;

public interface TaskView {
    void configListener();

    void setInitValues();

    void buildLayout();

    void refreshGridData();

    void setVisibleForm(Task task);

    void fillGridList();
}
