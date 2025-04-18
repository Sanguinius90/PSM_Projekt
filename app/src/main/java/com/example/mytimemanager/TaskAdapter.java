package com.example.mytimemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.date.setText(task.getDate());
        holder.checkBox.setChecked(task.isDone());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        CheckBox checkBox;

        public TaskViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.task_title);
            description = view.findViewById(R.id.task_description);
            date = view.findViewById(R.id.task_date);
            checkBox = view.findViewById(R.id.task_done);
        }
    }

    public void updateTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }
}
