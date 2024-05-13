package com.tmotions.wms.models.filtermodels;

public class LeaveList{
    public boolean Disabled;
    public Object Group;
    public boolean Selected;
    public String Text;
    public String Value;

    public boolean isDisabled() {
        return Disabled;
    }

    public void setDisabled(boolean disabled) {
        Disabled = disabled;
    }

    public Object getGroup() {
        return Group;
    }

    public void setGroup(Object group) {
        Group = group;
    }

    public boolean isSelected() {
        return Selected;
    }

    public void setSelected(boolean selected) {
        Selected = selected;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
