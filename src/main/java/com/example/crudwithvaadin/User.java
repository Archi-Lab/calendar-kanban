package com.example.crudwithvaadin;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String password;

    private Rolle rolle;

    private int distractionFactor;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String,Integer> sizeSettings = new HashMap<String, Integer>();


    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, LocalTime> timeSettings = new HashMap<String, LocalTime>();

    public User() {
        sizeSettings.put(Task.Size.S.name(),1);
        sizeSettings.put(Task.Size.M.name(),30);
        sizeSettings.put(Task.Size.L.name(),240);
        sizeSettings.put(Task.Size.XL.name(),(8*60));
        timeSettings.put("Monday",LocalTime.of(8,0));
        timeSettings.put("Tuesday",LocalTime.of(8,0));
        timeSettings.put("Wednesday",LocalTime.of(8,0));
        timeSettings.put("Thursday",LocalTime.of(8,0));
        timeSettings.put("Friday",LocalTime.of(8,0));
        timeSettings.put("Saturday", LocalTime.MIN);
        timeSettings.put("Sunday", LocalTime.MIN);
        distractionFactor=30;
    }

    public User(String name, String password, Rolle rolle) {
        sizeSettings.put(Task.Size.S.name(),1);
        sizeSettings.put(Task.Size.M.name(),30);
        sizeSettings.put(Task.Size.L.name(),240);
        sizeSettings.put(Task.Size.XL.name(),(8*60));
        timeSettings.put("Monday",LocalTime.of(8,0));
        timeSettings.put("Tuesday",LocalTime.of(8,0));
        timeSettings.put("Wednesday",LocalTime.of(8,0));
        timeSettings.put("Thursday",LocalTime.of(8,0));
        timeSettings.put("Friday",LocalTime.of(8,0));
        timeSettings.put("Saturday", LocalTime.MIN);
        timeSettings.put("Sunday", LocalTime.MIN);
        distractionFactor=30;
        this.username = name;
        this.password = password;
        this.rolle = rolle;
    }

    public String getName() {
        return username;
    }

    public void setName(String name) {
        this.username = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rolle getRolle() {
        return rolle;
    }

    public void setRolle(Rolle rolle) {
        this.rolle = rolle;
    }

    public Map<String, Integer> getSizeSettings() {
        return sizeSettings;
    }

    public Map<String, LocalTime> getTimeSettings() {
        return timeSettings;
    }

    public int getDistractionFactor() {
        return distractionFactor;
    }

    public void setDistractionFactor(int distractionFactor) {
        this.distractionFactor = distractionFactor;
    }

    public enum Rolle{
        NUTZER,ADMIN
    }
}
