package com.example.mytimemanager;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class GoalsFragment extends Fragment {

    private AppDatabase db;
    private GoalAdapter adapter;
    private List<GoalHistory> goals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_goals, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.goal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = AppDatabase.getInstance(requireContext());
        goals = db.goalDao().getAllGoals();

        adapter = new GoalAdapter(goals);
        recyclerView.setAdapter(adapter);

        // Swipe-to-delete z efektem tła i ikony
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                GoalHistory removedGoal = goals.get(position);

                db.goalDao().delete(removedGoal);
                goals.remove(position);
                adapter.notifyItemRemoved(position);

                Snackbar.make(recyclerView, "Cel usunięty", Snackbar.LENGTH_LONG)
                        .setAction("Cofnij", v -> {
                            db.goalDao().insert(removedGoal);
                            goals.add(position, removedGoal);
                            adapter.notifyItemInserted(position);
                        }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(ContextCompat.getColor(requireContext(), R.color.darkGreen));

                float radius = 50f;

                RectF rectF = new RectF(itemView.getRight() + dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                canvas.drawRoundRect(rectF, radius, radius, paint);

                Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                if (icon != null) {
                    icon.setTint(ContextCompat.getColor(requireContext(), android.R.color.white));
                    int iconSize = (int) (icon.getIntrinsicHeight() * 1.5);
                    int iconTop = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                    int iconLeft = itemView.getRight() - (itemView.getHeight() - iconSize) / 2 - iconSize;
                    icon.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
                    icon.draw(canvas);
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
        return view;
    }
}

