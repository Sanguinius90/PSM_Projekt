package com.example.mytimemanager;

import androidx.room.*;
import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM Task")
    List<Task> getAll();

    @Insert
    void insert(Task task);

    @Delete
    void delete(Task task);

    @Update
    void update(Task task);

    @Query("SELECT * FROM Task WHERE id = :id LIMIT 1")
    Task findById(int id);

    @Query("SELECT * FROM Task WHERE done = 1")
    List<Task> getDoneTasks();

    @Query("SELECT * FROM Task WHERE done = 0")
    List<Task> getActiveTasks();

    @Query("SELECT * FROM task WHERE done = 0")
    List<Task> getAllUnfinished();
}
