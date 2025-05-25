package com.example.mytimemanager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GoalHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public int target;
    public int completedCount;
    public String completedDate;
}
