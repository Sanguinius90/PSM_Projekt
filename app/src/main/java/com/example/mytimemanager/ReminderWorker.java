package com.example.mytimemanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("ReminderWorker", "Worker started");

        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .build();

        List<Task> tasks = db.taskDao().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        int notificationId = 1;

        for (Task task : tasks) {
            try {
                if (task.isDone() || task.isDeleted()) continue;

                Calendar taskDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                taskDate.setTime(sdf.parse(task.getDate()));

                if (taskDate.before(today)) continue;

                long diffInMillis = taskDate.getTimeInMillis() - today.getTimeInMillis();

                if (diffInMillis <= TimeUnit.HOURS.toMillis(24) && diffInMillis > 0) {
                    NotificationHelper.showNotification(
                            getApplicationContext(),
                            "Przypomnienie",
                            task.getTitle(),
                            notificationId++
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Result.success();
    }
}
