package com.example.mytimemanager;

import androidx.room.*;
import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM Task")
    List<Task> getAll();

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM task WHERE id = :taskId LIMIT 1")
    Task findById(int taskId);

    @Query("SELECT * FROM task WHERE deleted = 0 AND done = 1")
    List<Task> getDoneTasks();

    @Query("SELECT * FROM task WHERE deleted = 0 AND done = 0")
    List<Task> getActiveTasks();

    @Query("SELECT * FROM task WHERE deleted = 0 AND done = 0")
    List<Task> getAllUnfinished();

    @Query("SELECT * FROM task WHERE deleted = 1")
    List<Task> getDeletedTasks();
}
