package com.vanard.tulis;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

public class TodoRecyclerAdapter extends RecyclerView.Adapter<TodoRecyclerAdapter.ViewHolder> {

    private static final String TAG = "TodoRecyclerAdapter";
    private static final long milDay = 86400000;

    FirebaseFirestore db;

    private List<TodoLayout> todoLayoutList;
    private Context context;

    public TodoRecyclerAdapter(List<TodoLayout> todoLayoutList, Context context) {
        this.todoLayoutList = todoLayoutList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.todo_layout, parent, false);

        db = FirebaseFirestore.getInstance();

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final TodoLayout todoLayout = todoLayoutList.get(position);
        holder.setIsRecyclable(false);

        Calendar c = Calendar.getInstance();

        long deadline = todoLayout.getDate().getTime();
        long deadtime = todoLayout.getDeadtime();

        if (c.get(Calendar.DATE) == deadtime && (c.getTimeInMillis() - deadline) <= 0 && (c.getTimeInMillis() - deadline) > (-1*milDay)){
            Log.d(TAG, "onBindViewHolder: Today");
            Log.d(TAG, "onBindViewHolder: " + (c.getTimeInMillis()-deadline));
        }
        if (c.get(Calendar.DATE) - deadtime == -1 && (c.getTimeInMillis() - deadline) <= (-1*milDay)  && (c.getTimeInMillis() - deadline) >= (-2*milDay)){
            Log.d(TAG, "onBindViewHolder: Tomorrow");
            Log.d(TAG, "onBindViewHolder: " + (c.getTimeInMillis()-deadline));
        }
        if (c.get(Calendar.DATE) == deadtime && (c.getTimeInMillis() - deadline) <= (-1*milDay*27)) {
            Log.d(TAG, "onBindViewHolder: bulan depan");
            Log.d(TAG, "onBindViewHolder: " + (c.getTimeInMillis() - deadline));
        }
        if (c.get(Calendar.DATE) - deadtime < -1 && (c.getTimeInMillis() - deadline) <= (-1*milDay)){
            Log.d(TAG, "onBindViewHolder: Another day");
        }
        if (c.getTimeInMillis() - deadline > 0){
            Log.d(TAG, "onBindViewHolder: Terlewat");
        }

        String tName = todoLayout.getTodo_name();
        holder.todoName.setText(tName);

        String tDesc = todoLayout.getDescription();
        holder.todoDesc.setText(tDesc);
    }

    @Override
    public int getItemCount() {
        return todoLayoutList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView todoName;
        private TextView todoDesc;
        public ConstraintLayout viewBackground, viewForeground, viewDone;

        public ViewHolder(View itemView) {
            super(itemView);

            todoName = itemView.findViewById(R.id.todo_name);
            todoDesc = itemView.findViewById(R.id.todo_description);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
        }
    }

    public void removeItem(int position) {
        todoLayoutList.remove(position);
        notifyItemRemoved(position);
    }
}
