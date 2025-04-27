package com.example.mytimemanager;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.room.Room;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TaskWidget extends AppWidgetProvider {

    private static final String ACTION_MARK_DONE = "com.example.mytimemanager.ACTION_MARK_DONE";
    private static final String ACTION_DELETE_TASK = "com.example.mytimemanager.ACTION_DELETE_TASK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateTaskWidget(context, appWidgetManager);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .build();

        if (ACTION_MARK_DONE.equals(intent.getAction())) {
            int taskId = intent.getIntExtra("task_id", -1);
            if (taskId != -1) {
                Task task = db.taskDao().findById(taskId);
                if (task != null) {
                    task.setDone(true);
                    db.taskDao().update(task);
                }
            }
        } else if (ACTION_DELETE_TASK.equals(intent.getAction())) {
            int taskId = intent.getIntExtra("task_id", -1);
            if (taskId != -1) {
                Task task = db.taskDao().findById(taskId);
                if (task != null) {
                    db.taskDao().delete(task);
                }
            }
        }

        // Odśwież widżet
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        updateTaskWidget(context, appWidgetManager);
    }

    private static void updateTaskWidget(Context context, AppWidgetManager appWidgetManager) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_task);

        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .build();

        List<Task> tasks = db.taskDao().getAll();
        Task urgentTask = null;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        for (Task task : tasks) {
            if (!task.isDone()) {
                try {
                    Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    Calendar taskDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    taskDate.setTime(sdf.parse(task.getDate()));

                    long diffInMillis = taskDate.getTimeInMillis() - today.getTimeInMillis();
                    long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                    if (diffInDays <= 3) {
                        urgentTask = task;
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (urgentTask != null) {
            views.setTextViewText(R.id.widget_task_title, urgentTask.getTitle());

            // Przycisk "✔" - oznacz jako wykonane
            Intent doneIntent = new Intent(context, TaskWidget.class);
            doneIntent.setAction(ACTION_MARK_DONE);
            doneIntent.putExtra("task_id", urgentTask.getId());
            PendingIntent donePendingIntent = PendingIntent.getBroadcast(context, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_done_button, donePendingIntent);

            // Przycisk "🗑️" - usuń
            Intent deleteIntent = new Intent(context, TaskWidget.class);
            deleteIntent.setAction(ACTION_DELETE_TASK);
            deleteIntent.putExtra("task_id", urgentTask.getId());
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 1, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_delete_button, deletePendingIntent);

        } else {
            // Brak pilnych tasków
            views.setTextViewText(R.id.widget_task_title, "Brak pilnych zadań 📋");
            views.setOnClickPendingIntent(R.id.widget_done_button, null);
            views.setOnClickPendingIntent(R.id.widget_delete_button, null);
        }

        ComponentName widget = new ComponentName(context, TaskWidget.class);
        appWidgetManager.updateAppWidget(widget, views);
    }
}
