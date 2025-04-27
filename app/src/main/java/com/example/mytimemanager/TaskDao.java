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
}
