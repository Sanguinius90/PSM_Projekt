package com.example.mytimemanager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GoalDao {

    @Insert
    void insert(GoalHistory goal);

    @Query("SELECT * FROM GoalHistory ORDER BY id DESC")
    List<GoalHistory> getAllGoals();
}
