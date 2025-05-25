package com.example.mytimemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.mytimemanager.databinding.FragmentStatisticsBinding;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);

        db = Room.databaseBuilder(requireContext(),
                        AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item,
                getMonthNames()
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        binding.monthSpinner.setAdapter(adapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        binding.monthSpinner.setSelection(currentMonth);

        binding.monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateStats(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateStats(currentMonth);
        return binding.getRoot();
    }

    private void updateStats(int monthIndex) {
        List<Task> tasks = db.taskDao().getAll();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Date today = new Date();

        int total = 0;
        int done = 0;
        int overdue = 0;
        int deleted = 0;

        for (Task task : tasks) {
            try {
                Date taskDate = sdf.parse(task.getDate());
                if (taskDate == null) continue;
                cal.setTime(taskDate);
                int taskMonth = cal.get(Calendar.MONTH);

                if (taskMonth != monthIndex) continue;

                total++;

                if (task.isDeleted()) {
                    deleted++;
                    continue;
                }

                if (task.isDone()) {
                    done++;
                } else if (taskDate.before(today)) {
                    overdue++;
                }

            } catch (Exception ignored) {}
        }

        binding.totalTextView.setText("Zadania w tym miesiącu: " + total);
        binding.doneTextView.setText("Skończone: " + done);
        binding.overdueTextView.setText("Nieskończone: " + overdue);
        binding.deletedTextView.setText("Usunięte: " + deleted);
    }

    private List<String> getMonthNames() {
        return Arrays.asList("Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień");
    }
}
