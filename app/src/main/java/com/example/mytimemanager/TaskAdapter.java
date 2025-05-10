package com.example.mytimemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import android.graphics.Color;
import androidx.cardview.widget.CardView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

        if (task.isHighPriority()) {
            // Jeśli wysoki priorytet — bez względu na datę — zawsze czerwony
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        } else {
            // Jeśli NIE wysoki priorytet — sprawdzamy ile dni do deadline
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            try {
                Date today = new Date();
                Date taskDate = sdf.parse(task.getDate());

                if (taskDate != null) {
                    long diffInMillies = taskDate.getTime() - today.getTime();
                    long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies);

                    if (diffInDays <= 3) {
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
                    } else if (diffInDays <= 10) {
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange));
                    } else {
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(task);
            }
        });
    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        CheckBox checkBox;
        CardView cardView;

        public TaskViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.task_title);
            description = view.findViewById(R.id.task_description);
            date = view.findViewById(R.id.task_date);
            checkBox = view.findViewById(R.id.task_done);
            cardView = (CardView) view;
        }
    }

    public void updateTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
