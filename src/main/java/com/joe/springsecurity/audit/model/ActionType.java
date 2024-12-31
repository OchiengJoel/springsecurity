package com.joe.springsecurity.audit.model;

public enum ActionType {

    CREATE_ITEM("CREATE_ITEM"),
    UPDATE_ITEM("UPDATE_ITEM"),
    DELETE_ITEM("DELETE_ITEM"),
    CREATE_COMPANY("CREATE_COMPANY"),
    UPDATE_COMPANY("UPDATE_COMPANY"),
    DELETE_COMPANY("DELETE_COMPANY");

    private final String action;

    ActionType(String action){
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
