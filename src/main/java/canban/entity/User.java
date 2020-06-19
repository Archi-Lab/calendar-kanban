package canban.entity;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
public class User {

    @Id
    private UUID id = UUID.randomUUID();

    private String username;

    private String password;

    private Rolle rolle;

    private int distractionFactor;

    private int nweeksValue;

    private boolean connectGoogle;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String,Integer> sizeSettings = new HashMap<>();


    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, LocalTime> timeSettings = new HashMap<>();


    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, LocalTime> blockedTimeSettings = new HashMap<>();


    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, Integer> priorityHeightSettings = new HashMap<>();

    public User() {
        sizeSettings.put(Task.Size.S.name(),1);
        sizeSettings.put(Task.Size.M.name(),30);
        sizeSettings.put(Task.Size.L.name(),240);
        sizeSettings.put(Task.Size.XL.name(),(8*60));
        timeSettings.put("MONDAY",LocalTime.of(8,0));
        timeSettings.put("TUESDAY",LocalTime.of(8,0));
        timeSettings.put("WEDNESDAY",LocalTime.of(8,0));
        timeSettings.put("THURSDAY",LocalTime.of(8,0));
        timeSettings.put("FRIDAY",LocalTime.of(8,0));
        timeSettings.put("SATURDAY", LocalTime.MIN);
        timeSettings.put("SUNDAY", LocalTime.MIN);
        blockedTimeSettings.put("MONDAY",LocalTime.MIN);
        blockedTimeSettings.put("TUESDAY",LocalTime.MIN);
        blockedTimeSettings.put("WEDNESDAY",LocalTime.MIN);
        blockedTimeSettings.put("THURSDAY",LocalTime.MIN);
        blockedTimeSettings.put("FRIDAY",LocalTime.MIN);
        blockedTimeSettings.put("SATURDAY", LocalTime.MIN);
        blockedTimeSettings.put("SUNDAY", LocalTime.MIN);
        priorityHeightSettings.put(Task.Priority.LATER.name(),74);
        priorityHeightSettings.put(Task.Priority.NEXTNWEEK.name(),74);
        priorityHeightSettings.put(Task.Priority.NEXTWEEK.name(),74);
        priorityHeightSettings.put(Task.Priority.CURRENTWEEK.name(),74);
        priorityHeightSettings.put(Task.Priority.TODAY.name(),74);
        priorityHeightSettings.put(Task.Priority.NEARLYDONE.name(),74);
        priorityHeightSettings.put(Task.Priority.DONE.name(),74);
        distractionFactor=30;
        nweeksValue=1;
    }

    public User(String name, String password, Rolle rolle) {
        sizeSettings.put(Task.Size.S.name(),1);
        sizeSettings.put(Task.Size.M.name(),30);
        sizeSettings.put(Task.Size.L.name(),240);
        sizeSettings.put(Task.Size.XL.name(),(8*60));
        timeSettings.put("MONDAY",LocalTime.of(8,0));
        timeSettings.put("TUESDAY",LocalTime.of(8,0));
        timeSettings.put("WEDNESDAY",LocalTime.of(8,0));
        timeSettings.put("THURSDAY",LocalTime.of(8,0));
        timeSettings.put("FRIDAY",LocalTime.of(8,0));
        timeSettings.put("SATURDAY", LocalTime.MIN);
        timeSettings.put("SUNDAY", LocalTime.MIN);
        blockedTimeSettings.put("MONDAY",LocalTime.MIN);
        blockedTimeSettings.put("TUESDAY",LocalTime.MIN);
        blockedTimeSettings.put("WEDNESDAY",LocalTime.MIN);
        blockedTimeSettings.put("THURSDAY",LocalTime.MIN);
        blockedTimeSettings.put("FRIDAY",LocalTime.MIN);
        blockedTimeSettings.put("SATURDAY", LocalTime.MIN);
        blockedTimeSettings.put("SUNDAY", LocalTime.MIN);
        priorityHeightSettings.put(Task.Priority.LATER.name(),74);
        priorityHeightSettings.put(Task.Priority.NEXTNWEEK.name(),74);
        priorityHeightSettings.put(Task.Priority.NEXTWEEK.name(),74);
        priorityHeightSettings.put(Task.Priority.CURRENTWEEK.name(),74);
        priorityHeightSettings.put(Task.Priority.TODAY.name(),74);
        priorityHeightSettings.put(Task.Priority.NEARLYDONE.name(),74);
        priorityHeightSettings.put(Task.Priority.DONE.name(),74);
        distractionFactor=30;
        nweeksValue=1;
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

    public Map<String, LocalTime> getBlockedTimeSettings() {
        return blockedTimeSettings;
    }

    public int getDistractionFactor() {
        return distractionFactor;
    }

    public void setDistractionFactor(int distractionFactor) {
        this.distractionFactor = distractionFactor;
    }

    public int getNweeksValue() {
        return nweeksValue;
    }

    public void setNweeksValue(int nweeksValue) {
        this.nweeksValue = nweeksValue;
    }

    public boolean isConnectGoogle() {
        return connectGoogle;
    }

    public void setConnectGoogle(boolean connectGoogle) {
        this.connectGoogle = connectGoogle;
    }

    public Map<String, Integer> getPriorityHeightSettings() {
        return priorityHeightSettings;
    }

    public UUID getId() {
        return id;
    }

    public enum Rolle{
        NUTZER,ADMIN
    }
}
