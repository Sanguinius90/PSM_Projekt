package com.example.mytimemanager;

import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import com.example.mytimemanager.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ustawienie toolbaru
        setSupportActionBar(binding.toolbar);

        // Ustawienie kliknięcia przycisku
        binding.addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            // Pobieramy wartości
                            String title = ((android.widget.EditText) dialogView.findViewById(R.id.input_title)).getText().toString();
                            String description = ((android.widget.EditText) dialogView.findViewById(R.id.input_description)).getText().toString();
                            String date = ((android.widget.EditText) dialogView.findViewById(R.id.input_date)).getText().toString();

                            if (!title.isEmpty()) {
                                Task newTask = new Task(title, description, date, false);
                                taskList.add(newTask);
                                adapter.notifyItemInserted(taskList.size() - 1);
                                Snackbar.make(view, "Dodano zadanie", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Tytuł nie może być pusty!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
            }
        });

        // RecyclerView i adapter
        RecyclerView recyclerView = binding.taskRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);

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
                taskList.remove(position);
                adapter.notifyItemRemoved(position);

                Snackbar.make(binding.getRoot(), "Usunięto zadanie", Snackbar.LENGTH_LONG)
                        .setAction("Cofnij", v -> {
                            taskList.add(position, removedTask);
                            adapter.notifyItemInserted(position);
                        }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(ContextCompat.getColor(MainActivity.this, R.color.darkGreen));

                // Rysowanie tła
                if (dX < 0) { // Swipe w lewo
                    canvas.drawRect(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom(), paint);

                    // Ikona kosza
                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    if (icon != null) {
                        icon.setTint(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                    }
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
        });
        itemTouchHelper.attachToRecyclerView(binding.taskRecyclerView);

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
