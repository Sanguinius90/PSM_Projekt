package com.example.mytimemanager;

import android.Manifest;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import com.example.mytimemanager.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationHelper.createNotificationChannel(this);

        //Sprawdzanie czy wysłać powiadomienie co 24h

//        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
//                ReminderWorker.class,
//                24, TimeUnit.HOURS
//        ).build();
//
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//                "task_reminder",
//                ExistingPeriodicWorkPolicy.KEEP,
//                request
//        );

        // Powiadomienie przy uruchamianiu aplikacji
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class).build();
        WorkManager.getInstance(this).enqueue(request);
//

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicjalizacja Room
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        // Toolbar
        setSupportActionBar(binding.toolbar);

        // Pobranie zadań z bazy
        taskList = db.taskDao().getAll();

        // Ustawienie RecyclerView i adaptera
        RecyclerView recyclerView = binding.taskRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);

        // Dodawanie zadania
        binding.addTask.setOnClickListener(view -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
            EditText dateInput = dialogView.findViewById(R.id.input_date);

            dateInput.setOnClickListener(v -> {
                MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Wybierz datę")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

                datePicker.addOnPositiveButtonClickListener(selection -> {
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    calendar.setTimeInMillis(selection);

                    String selectedDate = String.format(
                            "%02d.%02d.%04d",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR)
                    );

                    dateInput.setText(selectedDate);
                });

                datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            });

            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Dodaj zadanie")
                    .setView(dialogView)
                    .setPositiveButton("Dodaj", (dialog, which) -> {
                        String title = ((EditText) dialogView.findViewById(R.id.input_title)).getText().toString();
                        String description = ((EditText) dialogView.findViewById(R.id.input_description)).getText().toString();
                        String date = ((EditText) dialogView.findViewById(R.id.input_date)).getText().toString();
                        boolean highPriority = ((CheckBox) dialogView.findViewById(R.id.input_high_priority)).isChecked();

                        if (!title.isEmpty()) {
                            Task newTask = new Task(title, description, date, false, highPriority);
                            db.taskDao().insert(newTask);
                            taskList = db.taskDao().getAll();
                            adapter.updateTasks(taskList);
                            Snackbar.make(view, "Dodano zadanie", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Tytuł nie może być pusty!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });

        // Swipe do usuwania
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task removedTask = taskList.get(position);
                db.taskDao().delete(removedTask);
                taskList = db.taskDao().getAll();
                adapter.updateTasks(taskList);

                Snackbar.make(binding.getRoot(), "Usunięto zadanie", Snackbar.LENGTH_LONG)
                        .setAction("Cofnij", v -> {
                            db.taskDao().insert(removedTask);
                            taskList = db.taskDao().getAll();
                            adapter.updateTasks(taskList);
                        }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();

                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < taskList.size()) {
                    Task task = taskList.get(position);

                    int colorResId = R.color.darkGreen; // Domyślnie ciemnozielony

                    if (task.isHighPriority()) {
                        // Jeśli wysoki priorytet - zawsze ciemnoczerwony
                        colorResId = R.color.darkRed;
                    } else {
                        // Jeśli nie wysoki priorytet - sprawdzamy ile dni do deadline
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        try {
                            Date today = new Date();
                            Date taskDate = sdf.parse(task.getDate());

                            if (taskDate != null) {
                                long diffInMillies = taskDate.getTime() - today.getTime();
                                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies);

                                if (diffInDays <= 3) {
                                    colorResId = R.color.darkRed;
                                } else if (diffInDays <= 10) {
                                    colorResId = R.color.darkOrange;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    paint.setColor(ContextCompat.getColor(MainActivity.this, colorResId));
                }

                if (dX < 0) {
                    float radius = 50f;

                    RectF rectF = new RectF(
                            itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom()
                    );
                    canvas.drawRoundRect(rectF, radius, radius, paint);

                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    if (icon != null) {
                        icon.setTint(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                        float scale = 1.5f;
                        int iconWidth = (int) (icon.getIntrinsicWidth() * scale);
                        int iconHeight = (int) (icon.getIntrinsicHeight() * scale);
                        int iconTop = itemView.getTop() + (itemView.getHeight() - iconHeight) / 2;
                        int iconLeft = itemView.getRight() - (itemView.getHeight() - iconHeight) / 2 - iconWidth;
                        int iconRight = iconLeft + iconWidth;
                        int iconBottom = iconTop + iconHeight;
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(canvas);
                    }
                }
            }

        });

        itemTouchHelper.attachToRecyclerView(binding.taskRecyclerView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflacja menu - dodanie elementów do paska akcji
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Obsługa kliknięć na elementy menu
        int id = item.getItemId();

        // Jeśli kliknięto opcję ustawień
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Przejdź do ustawień", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
