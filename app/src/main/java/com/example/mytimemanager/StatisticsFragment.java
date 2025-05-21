package com.example.mytimemanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.mytimemanager.databinding.FragmentStatisticsBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        db = Room.databaseBuilder(requireContext(),
                        AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        List<String> monthNames = getMonthNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, monthNames
        );
        binding.monthSpinner.setAdapter(adapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        binding.monthSpinner.setSelection(currentMonth);
        showPieChart(currentMonth);

        binding.monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showPieChart(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void showPieChart(int monthIndex) {
        List<Task> all = db.taskDao().getAll();

        int done = 0, active = 0, overdue = 0;
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        for (Task t : all) {
            if (t.isDeleted()) continue;

            try {
                Date taskDate = sdf.parse(t.getDate());
                if (taskDate == null) continue;

                calendar.setTime(taskDate);
                int taskMonth = calendar.get(Calendar.MONTH);

                if (taskMonth != monthIndex) continue;

                if (t.isDone()) done++;
                else if (taskDate.before(today)) overdue++;
                else active++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PieChart pieChart = binding.pieChart;
        List<PieEntry> entries = new ArrayList<>();
        if (done + active + overdue == 0) {
            entries.add(new PieEntry(100, "Brak danych"));
        } else {
            if (done > 0) entries.add(new PieEntry(done, "Skończone"));
            if (active > 0) entries.add(new PieEntry(active, "Aktywne"));
            if (overdue > 0) entries.add(new PieEntry(overdue, "Nieskończone"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setDrawValues(false);

        if (done + active + overdue == 0) {
            dataSet.setColors(Collections.singletonList(ContextCompat.getColor(requireContext(), R.color.green)));
        } else {
            dataSet.setColors(
                    ContextCompat.getColor(requireContext(), R.color.green),
                    ContextCompat.getColor(requireContext(), R.color.orange),
                    ContextCompat.getColor(requireContext(), R.color.red)
            );
        }

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setCenterText("Zadania");
        pieChart.setCenterTextSize(20f);
        pieChart.setDrawCenterText(true);
        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);
        legend.setTextColor(Color.BLACK);

        pieChart.invalidate();
    }

    private List<String> getMonthNames() {
        return Arrays.asList("Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
                "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień");
    }
}
