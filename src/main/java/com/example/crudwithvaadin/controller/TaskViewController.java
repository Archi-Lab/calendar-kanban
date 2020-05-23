package com.example.crudwithvaadin.controller;

import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.view.TaskViewImpl;

public class TaskViewController {

    private TaskViewImpl view;

    public TaskViewController(TaskViewImpl view) {
        this.view = view;
    }

    public void onEnter() {
        view.buildLayout();
        view.fillGridList();
        view.configListener();
        view.setInitValues();
        view.refreshGridData();
    }

    public void taskClicked(Task task){
        this.view.setVisibleForm(task);
    }
}
