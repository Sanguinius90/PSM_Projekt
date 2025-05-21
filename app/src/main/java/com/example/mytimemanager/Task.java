package com.example.mytimemanager;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private String date;
    private boolean done;
    private boolean highPriority;
    private boolean deleted;

    public Task(String title, String description, String date, boolean done, boolean highPriority) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.done = done;
        this.highPriority = highPriority;
        this.deleted = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isHighPriority() {return highPriority;}

    public boolean isDeleted() { return deleted; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setHighPriority(boolean highPriority) {this.highPriority = highPriority;}

    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
