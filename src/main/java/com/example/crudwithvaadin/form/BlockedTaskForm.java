package com.example.crudwithvaadin.form;

import authentication.CurrentUser;
import com.example.crudwithvaadin.view.SettingsView;
import com.example.crudwithvaadin.entity.BlockedTask;
import com.example.crudwithvaadin.repository.BlockedTaskRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;

public class BlockedTaskForm extends Div {

    private BlockedTaskRepository blockedTaskRepository;
    private TextField beschreibung;
    private DatePicker datePicker;
    private TimePicker startTimePicker;
    private TimePicker endTimePicker;
    private Button abort = new Button("Abbrechen");
    private Button delete = new Button("Löschen");
    private Button save = new Button("Speichern");
    private SettingsView settingsView;
    private BlockedTask blockedTask;

    VerticalLayout content;

    public BlockedTaskForm(SettingsView settingsView, BlockedTaskRepository blockedTaskRepository) {
        this.blockedTaskRepository = blockedTaskRepository;
        this.settingsView=settingsView;
        buildLayout();
    }

    private void buildLayout(){

        beschreibung = new TextField("Beschreibung");
        datePicker = new DatePicker("Datum");
        startTimePicker = new TimePicker("Start");
        endTimePicker = new TimePicker("Ende");


        setClassName("product-form");
        content = new VerticalLayout();
        content.setSizeUndefined();
        add(content);

        abort.addClickListener(e->{
           this.setVisible(false);
        });

        save.addClickListener(e->{
            this.setVisible(false);
            if(this.blockedTask==null){
                this.blockedTask=new BlockedTask();
                this.blockedTask.setUser(CurrentUser.getRole());
            }
            this.blockedTask.setBeschreibung(beschreibung.getValue());
            this.blockedTask.setDate(datePicker.getValue());
            this.blockedTask.setStartTime(startTimePicker.getValue());
            this.blockedTask.setEndTime(endTimePicker.getValue());

            this.blockedTaskRepository.save(this.blockedTask);
            this.settingsView.refreshGrid();
        });

        delete.addClickListener(e->{
            this.setVisible(false);
            if(this.blockedTask!=null) {
                this.blockedTaskRepository.delete(this.blockedTask);
            }
            this.settingsView.refreshGrid();
        });

        content.add(beschreibung,datePicker,startTimePicker,endTimePicker,save,abort,delete);
    }

    public void fillForm(BlockedTask blockedTask) {
        this.blockedTask = blockedTask;
        if(this.blockedTask==null){
            this.beschreibung.setValue("");
            this.datePicker.setValue(null);
            this.startTimePicker.setValue(null);
            this.endTimePicker.setValue(null);
        }else{
            this.beschreibung.setValue(this.blockedTask.getBeschreibung());
            this.datePicker.setValue(this.blockedTask.getDate());
            this.startTimePicker.setValue(this.blockedTask.getStartTime());
            this.endTimePicker.setValue(this.blockedTask.getEndTime());
        }
    }
}