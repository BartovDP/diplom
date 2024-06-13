package com.example.Entities;

public class GroupDetails {
    private int groupId;
    private int position;
    private String groupName;
    private int tagId;

    public GroupDetails(int groupId, int position, String groupName, int tagId) {
        this.groupId = groupId;
        this.position = position;
        this.groupName = groupName;
        this.tagId = tagId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getPosition() {
        return position;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getTagId() {
        return tagId;
    }
}
