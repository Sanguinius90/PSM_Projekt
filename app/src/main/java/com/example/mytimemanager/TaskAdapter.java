package com.example.mytimemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final AppDatabase db;
    private final Context context;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
        this.db = Room.databaseBuilder(context, AppDatabase.class, "task-database")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
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

        // Resetujemy listener przed ustawieniem stanu
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isDone());

        // Obsługa kliknięcia checkboxa
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setDone(isChecked);
            db.taskDao().update(task);

            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                taskList.remove(pos);
                notifyItemRemoved(pos);
            }
        });

        // Kolor karty zależny od priorytetu i terminu
        if (task.isHighPriority()) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            try {
                Date today = new Date();
                Date taskDate = sdf.parse(task.getDate());

                if (taskDate != null) {
                    long diffInMillies = taskDate.getTime() - today.getTime();
                    long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies);

                    if (diffInDays <= 3) {
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
                    } else if (diffInDays <= 10) {
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange));
                    } else {
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green));
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Obsługa kliknięcia w kartę (np. do edycji)
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

    public void updateTasks(List<Task> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
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

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
