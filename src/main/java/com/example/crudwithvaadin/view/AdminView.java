package com.example.crudwithvaadin.view;

import com.example.crudwithvaadin.entity.User;

public interface AdminView {
    void configListener();

    void buildLayout();

    void refreshGrid();

    void setVisibleForm(User user);
}
