package canban.view;

import canban.entity.User;

public interface AdminView {
    void configListener();

    void buildLayout();

    void refreshGrid();

    void setVisibleForm(User user);
}
