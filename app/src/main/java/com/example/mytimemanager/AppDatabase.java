package com.example.mytimemanager;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
}
