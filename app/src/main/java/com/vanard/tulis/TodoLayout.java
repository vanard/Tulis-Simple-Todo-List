package com.vanard.tulis;

import java.util.Date;

public class TodoLayout {
    private String user_id, todo_name, description, deadline, priority, time, category_title, category_documentId;
    private long milis, deadtime;
    private int high;
    private Date date;
    private Date timestamp;
    private String documentId;

    public TodoLayout() {
    }

    public TodoLayout(String user_id, String todo_name, String description, String deadline, String priority, String time, String category_title, String category_documentId, long milis, long deadtime, int high, Date date, Date timestamp, String documentId) {
        this.user_id = user_id;
        this.todo_name = todo_name;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.time = time;
        this.category_title = category_title;
        this.category_documentId = category_documentId;
        this.milis = milis;
        this.deadtime = deadtime;
        this.high = high;
        this.date = date;
        this.timestamp = timestamp;
        this.documentId = documentId;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTodo_name() {
        return todo_name;
    }

    public void setTodo_name(String todo_name) {
        this.todo_name = todo_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCategory_title() {
        return category_title;
    }

    public void setCategory_title(String category_title) {
        this.category_title = category_title;
    }

    public String getCategory_documentId() {
        return category_documentId;
    }

    public void setCategory_documentId(String category_documentId) {
        this.category_documentId = category_documentId;
    }

    public long getMilis() {
        return milis;
    }

    public void setMilis(long milis) {
        this.milis = milis;
    }

    public long getDeadtime() {
        return deadtime;
    }

    public void setDeadtime(long deadtime) {
        this.deadtime = deadtime;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
