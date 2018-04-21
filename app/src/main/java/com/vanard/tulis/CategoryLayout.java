package com.vanard.tulis;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class CategoryLayout {
    private String user_id, category_title;
    private Date timestamp;
    private String documentId;

    public CategoryLayout() {
    }

    public CategoryLayout(String user_id, String category_title, Date timestamp, String documentId) {
        this.user_id = user_id;
        this.category_title = category_title;
        this.timestamp = timestamp;
        this.documentId = documentId;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCategory_title() {
        return category_title;
    }

    public void setCategory_title(String category_title) {
        this.category_title = category_title;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}