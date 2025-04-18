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

    public Task(String title, String description, String date, boolean done) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.done = done;
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
}
