package com.example.mytimemanager;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.mytimemanager.databinding.ActivityMainBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter adapter;
    private AppDatabase db;
    private SharedPreferences prefs;
    private static final String PREF_GOAL_TEXT = "goal_text";
    private static final String PREF_GOAL_TARGET = "goal_target";

    private enum ViewMode { ACTIVE, DONE }
    private ViewMode currentView = ViewMode.ACTIVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        prefs = getSharedPreferences("goal_prefs", MODE_PRIVATE);

        NotificationHelper.createNotificationChannel(this);
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ReminderWorker.class).build();
        WorkManager.getInstance(this).enqueue(request);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        binding.goalContainer.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_goal, null);
            EditText goalInput = dialogView.findViewById(R.id.goal_input);
            EditText targetInput = dialogView.findViewById(R.id.target_input);

            new AlertDialog.Builder(this)
                    .setTitle("Ustaw cel")
                    .setView(dialogView)
                    .setPositiveButton("Zapisz", (dialog, which) -> {
                        String goalText = goalInput.getText().toString();
                        int target = Integer.parseInt(targetInput.getText().toString());

                        prefs.edit()
                                .putString(PREF_GOAL_TEXT, goalText)
                                .putInt(PREF_GOAL_TARGET, target)
                                .apply();

                        updateGoalView();
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });

        updateGoalView();
        setSupportActionBar(binding.toolbar);

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_active) {
                currentView = ViewMode.ACTIVE;
                taskList = db.taskDao().getActiveTasks();
                filterOnlyUpcomingTasks(taskList);
                sortTasksByDate(taskList);

                adapter = new TaskAdapter(taskList, this, false);
                adapter.setOnTaskStatusChangedListener(() -> updateGoalView());
                adapter.setOnItemClickListener(task -> showEditDialog(task));
                binding.taskRecyclerView.setAdapter(adapter);

                binding.taskRecyclerView.setVisibility(View.VISIBLE);
                binding.fragmentContainer.setVisibility(View.GONE);
                binding.addTask.show();

            } else if (id == R.id.nav_done) {
                currentView = ViewMode.DONE;
                taskList = db.taskDao().getDoneTasks();
                sortTasksByDate(taskList);

                adapter = new TaskAdapter(taskList, this, false);
                adapter.setOnTaskStatusChangedListener(() -> updateGoalView());
                adapter.setOnItemClickListener(task -> showEditDialog(task));
                binding.taskRecyclerView.setAdapter(adapter);

                binding.taskRecyclerView.setVisibility(View.VISIBLE);
                binding.fragmentContainer.setVisibility(View.GONE);
                binding.addTask.hide();

            } else if (id == R.id.nav_late) {
                currentView = null;
                taskList = db.taskDao().getAllUnfinished();
                filterOverdueTasks(taskList);
                sortTasksByDate(taskList);

                adapter = new TaskAdapter(taskList, this, false);
                adapter.setOnTaskStatusChangedListener(() -> updateGoalView());
                adapter.setOnItemClickListener(task -> showEditDialog(task));
                binding.taskRecyclerView.setAdapter(adapter);

                binding.taskRecyclerView.setVisibility(View.VISIBLE);
                binding.fragmentContainer.setVisibility(View.GONE);
                binding.addTask.hide();

            } else if (id == R.id.nav_deleted) {
                currentView = null;
                taskList = db.taskDao().getDeletedTasks();
                sortTasksByDate(taskList);

                adapter = new TaskAdapter(taskList, this, true); // <- isDeletedTab = true!
                adapter.setOnItemClickListener(task -> showEditDialog(task));
                binding.taskRecyclerView.setAdapter(adapter);

                binding.taskRecyclerView.setVisibility(View.VISIBLE);
                binding.fragmentContainer.setVisibility(View.GONE);
                binding.addTask.hide();

            } else if (id == R.id.nav_stats) {
                binding.fragmentContainer.setVisibility(View.VISIBLE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new StatisticsFragment())
                        .addToBackStack(null)
                        .commit();

                binding.taskRecyclerView.setVisibility(View.GONE);
                binding.addTask.hide();

            } else if (id == R.id.nav_goals) {
                binding.fragmentContainer.setVisibility(View.VISIBLE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new GoalsFragment())
                        .addToBackStack(null)
                        .commit();

                binding.taskRecyclerView.setVisibility(View.GONE);
                binding.addTask.hide();
            }

            binding.drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        taskList = db.taskDao().getActiveTasks();
        filterOnlyUpcomingTasks(taskList);
        sortTasksByDate(taskList);
        adapter = new TaskAdapter(taskList, this, false);
        adapter.setOnTaskStatusChangedListener(() -> updateGoalView());
        adapter.setOnItemClickListener(task -> showEditDialog(task));

        RecyclerView recyclerView = binding.taskRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && binding.addTask.getVisibility() == View.VISIBLE) {
                    binding.addTask.hide();
                } else if (dy < 0 && binding.addTask.getVisibility() != View.VISIBLE) {
                    binding.addTask.show();
                }
            }
        });

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
                            calendar.get(Calendar.YEAR));
                    dateInput.setText(selectedDate);
                });

                datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            });

            new AlertDialog.Builder(this)
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
                            taskList = db.taskDao().getActiveTasks();
                            filterOnlyUpcomingTasks(taskList);
                            sortTasksByDate(taskList);
                            adapter.updateTasks(taskList);
                        } else {
                            Toast.makeText(this, "Tytuł nie może być pusty!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (currentView == null) {
                    // Jesteś w zakładce „Usunięte” – pozwól na swipe w obie strony
                    return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
                } else {
                    // Inne zakładki – tylko swipe w lewo
                    return makeMovementFlags(0, ItemTouchHelper.LEFT);
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task swipedTask = taskList.get(position);

                if (currentView == null) { // Usunięte
                    if (direction == ItemTouchHelper.RIGHT) {
                        // PRZYWRÓĆ ZADANIE
                        swipedTask.setDeleted(false);
                        db.taskDao().update(swipedTask);
                        updateDeletedTasks();

                        Snackbar.make(binding.getRoot(), "Przywrócono zadanie", Snackbar.LENGTH_LONG)
                                .setAction("Cofnij", v -> {
                                    swipedTask.setDeleted(true);
                                    db.taskDao().update(swipedTask);
                                    updateDeletedTasks();
                                }).show();
                    } else {
                        // OPCJONALNIE: permanentne usunięcie
                        db.taskDao().delete(swipedTask);
                        updateDeletedTasks();

                        Snackbar.make(binding.getRoot(), "Usunięto na stałe", Snackbar.LENGTH_LONG)
                                .setAction("Cofnij", v -> {
                                    db.taskDao().insert(swipedTask);
                                    updateDeletedTasks();
                                }).show();
                    }
                } else {
                    // Aktywne / Skończone → oznacz jako deleted
                    swipedTask.setDeleted(true);
                    db.taskDao().update(swipedTask);
                    updateTaskList();

                    Snackbar.make(binding.getRoot(), "Usunięto zadanie", Snackbar.LENGTH_LONG)
                            .setAction("Cofnij", v -> {
                                swipedTask.setDeleted(false);
                                db.taskDao().update(swipedTask);
                                updateTaskList();
                            }).show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();

                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < taskList.size()) {
                    Task task = taskList.get(position);
                    int colorResId = R.color.darkGreen;

                    if (task.isHighPriority()) {
                        colorResId = R.color.darkRed;
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        try {
                            Date today = new Date();
                            Date taskDate = sdf.parse(task.getDate());
                            if (taskDate != null) {
                                long diffInDays = TimeUnit.MILLISECONDS.toDays(taskDate.getTime() - today.getTime());
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

                float radius = 50f;
                RectF rectF;

                if (dX < 0) {
                    // Swipe w lewo (usuń)
                    rectF = new RectF(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    canvas.drawRoundRect(rectF, radius, radius, paint);

                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    if (icon != null) {
                        icon.setTint(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                        int iconSize = (int) (icon.getIntrinsicHeight() * 1.5);
                        int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                        int iconLeft = itemView.getRight() - (itemView.getHeight() - iconSize) / 2 - iconSize;
                        icon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
                        icon.draw(canvas);
                    }

                } else if (dX > 0 && currentView == null) {
                    // Swipe w prawo (przywróć) – tylko tło, bez ikonki
                    rectF = new RectF(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + dX, itemView.getBottom());
                    canvas.drawRoundRect(rectF, radius, radius, paint);
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        itemTouchHelper.attachToRecyclerView(binding.taskRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_drawer_open) {
            binding.drawerLayout.openDrawer(GravityCompat.END);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditDialog(Task task) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        EditText titleInput = dialogView.findViewById(R.id.input_title);
        EditText descriptionInput = dialogView.findViewById(R.id.input_description);
        EditText dateInput = dialogView.findViewById(R.id.input_date);
        CheckBox highPriorityInput = dialogView.findViewById(R.id.input_high_priority);

        titleInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());
        dateInput.setText(task.getDate());
        highPriorityInput.setChecked(task.isHighPriority());

        dateInput.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Wybierz datę")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                String selectedDate = String.format("%02d.%02d.%04d",
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.YEAR));
                dateInput.setText(selectedDate);
            });

            datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });

        new AlertDialog.Builder(this)
                .setTitle("Edytuj zadanie")
                .setView(dialogView)
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    task.setTitle(titleInput.getText().toString());
                    task.setDescription(descriptionInput.getText().toString());
                    task.setDate(dateInput.getText().toString());
                    task.setHighPriority(highPriorityInput.isChecked());

                    db.taskDao().update(task);
                    updateTaskList();
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void updateTaskList() {
        if (currentView == ViewMode.ACTIVE) {
            taskList = db.taskDao().getActiveTasks();
            filterOnlyUpcomingTasks(taskList);
        } else if (currentView == ViewMode.DONE) {
            taskList = db.taskDao().getDoneTasks();
        }
        sortTasksByDate(taskList);
        adapter.updateTasks(taskList);
        updateGoalView();
    }

    private void updateGoalView() {
        if (db == null) return;

        String text = prefs.getString(PREF_GOAL_TEXT, null);
        int target = prefs.getInt(PREF_GOAL_TARGET, 0);
        int done = db.taskDao().getDoneTasks().size();

        if (text != null && target > 0) {
            binding.goalText.setText(text + " (" + done + "/" + target + ")");
            binding.goalProgress.setMax(target);
            binding.goalProgress.setProgress(Math.min(done, target));

            if (done >= target) {
                GoalHistory goal = new GoalHistory();
                goal.title = text;
                goal.target = target;
                goal.completedCount = done;
                goal.completedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());

                db.goalDao().insert(goal);
                prefs.edit().remove(PREF_GOAL_TEXT).remove(PREF_GOAL_TARGET).apply();

                Toast.makeText(this, "🎉 Cel ukończony i zapisany!", Toast.LENGTH_LONG).show();

                // Wyzeruj widok
                binding.goalText.setText("Brak celu. Kliknij, aby ustawić");
                binding.goalProgress.setProgress(0);
            }

        } else {
            binding.goalText.setText("Brak celu. Kliknij, aby ustawić");
            binding.goalProgress.setProgress(0);
        }
    }

    private void updateLateTasks() {
        taskList = db.taskDao().getAllUnfinished();
        filterOverdueTasks(taskList);
        sortTasksByDate(taskList);
        adapter.updateTasks(taskList);
    }

    private void sortTasksByDate(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        tasks.sort((t1, t2) -> {
            try {
                if (t1.isHighPriority() && !t2.isHighPriority()) return -1;
                if (!t1.isHighPriority() && t2.isHighPriority()) return 1;

                Date d1 = sdf.parse(t1.getDate());
                Date d2 = sdf.parse(t2.getDate());
                return Objects.requireNonNull(d1).compareTo(d2);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    private void filterOverdueTasks(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date today = new Date();

        tasks.removeIf(task -> {
            try {
                Date taskDate = sdf.parse(task.getDate());
                return taskDate == null || !taskDate.before(today);
            } catch (Exception e) {
                return true;
            }
        });
    }

    private void filterOnlyUpcomingTasks(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Date today = new Date();

        tasks.removeIf(task -> {
            try {
                Date taskDate = sdf.parse(task.getDate());
                return taskDate != null && taskDate.before(today);
            } catch (Exception e) {
                return true;
            }
        });
    }

    private void updateDeletedTasks() {
        taskList = db.taskDao().getDeletedTasks();
        sortTasksByDate(taskList);
        adapter.updateTasks(taskList);
    }
}