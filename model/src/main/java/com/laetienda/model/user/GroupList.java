package com.laetienda.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupList {

    private Map<String, Group> groups;

    public GroupList(){
        groups = new HashMap<>();
    }
    public GroupList(Map<String, Group> groups) {
        this.groups = groups;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    public GroupList addGroup(Group group){
        if(groups == null){
            groups = new HashMap<>();
        }

        groups.put(group.getName(), group);
        return this;
    }
}
