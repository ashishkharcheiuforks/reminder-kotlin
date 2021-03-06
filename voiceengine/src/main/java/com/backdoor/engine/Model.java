package com.backdoor.engine;

import com.backdoor.engine.misc.Action;
import com.backdoor.engine.misc.ActionType;

import java.util.ArrayList;
import java.util.List;

public class Model {

    private String target;
    private String summary;
    private String dateTime;
    private long repeatInterval;
    private ActionType type;
    private List<Integer> weekdays = new ArrayList<>();
    private Action action;
    private boolean hasCalendar;

    public Model() {
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public List<Integer> getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(List<Integer> weekdays) {
        this.weekdays = weekdays;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean isHasCalendar() {
        return hasCalendar;
    }

    public void setHasCalendar(boolean hasCalendar) {
        this.hasCalendar = hasCalendar;
    }

    @Override
    public String toString() {
        return "Model{" +
                "target='" + target + '\'' +
                ", summary='" + summary + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", repeatInterval=" + repeatInterval +
                ", type=" + type +
                ", weekdays=" + weekdays.toString() +
                ", action=" + action +
                ", hasCalendar=" + hasCalendar +
                '}';
    }
}
