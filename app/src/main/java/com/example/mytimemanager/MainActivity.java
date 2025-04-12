package com.example.mytimemanager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import com.example.mytimemanager.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

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
                // Dodaj nowe zadanie
                Task newTask = new Task("Nowe zadanie", "Opis zadania", "12.04.2025", false);
                taskList.add(newTask);
                adapter.notifyItemInserted(taskList.size() - 1);
                Snackbar.make(view, "Dodano nowe zadanie", Snackbar.LENGTH_SHORT).show();
            }
        });

        // RecyclerView i adapter
        RecyclerView recyclerView = binding.taskRecyclerView;  // Użycie binding
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);
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
