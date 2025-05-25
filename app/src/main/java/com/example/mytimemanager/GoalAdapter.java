package com.example.mytimemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
    private List<GoalHistory> goalList;

    public GoalAdapter(List<GoalHistory> goals) {
        this.goalList = goals;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalHistory goal = goalList.get(position);
        holder.title.setText(goal.title);
        holder.description.setText("Zadania wykonane: " + goal.completedCount + " / " + goal.target);
        holder.date.setText(goal.completedDate);
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;

        public GoalViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.goal_title);
            description = view.findViewById(R.id.goal_description);
            date = view.findViewById(R.id.goal_date);
        }
    }
}
