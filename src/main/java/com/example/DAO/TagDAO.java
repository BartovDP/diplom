package com.example.DAO;

import java.util.List;

public interface TagDAO {
    List<String> getAllTags();
    int getTagId(String tagName);
    String getTagNameById(int tagId);
    String getTagColor(String tagName);
    List<String> getTagsForTask(int taskId);
    void saveTagToTask(String tagName, int taskId);
    void removeAllTagsFromTask(int taskId);
}
