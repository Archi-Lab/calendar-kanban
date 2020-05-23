package com.example.crudwithvaadin.controller;

import com.example.crudwithvaadin.entity.Task;
import com.example.crudwithvaadin.entity.User;
import com.example.crudwithvaadin.view.AdminView;

public class AdminViewController {

    private AdminView view;
    public AdminViewController(AdminView adminView) {
        this.view=adminView;
    }

    public void onEnter() {
        view.buildLayout();
        view.configListener();
    }

    public void userClicked(User user){
        this.view.setVisibleForm(user);
    }
}
