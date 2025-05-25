package com.example.mytimemanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GoalsFragment extends Fragment {

    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.goal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = AppDatabase.getInstance(requireContext());
        List<GoalHistory> goals = db.goalDao().getAllGoals();

        GoalAdapter adapter = new GoalAdapter(goals);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
