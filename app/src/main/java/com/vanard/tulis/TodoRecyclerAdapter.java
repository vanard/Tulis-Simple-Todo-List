package com.vanard.tulis;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TodoRecyclerAdapter extends RecyclerView.Adapter<TodoRecyclerAdapter.ViewHolder> {

    private static final String TAG = "TodoRecyclerAdapter";
//    private static final long milDay = 86400000;

    FirebaseFirestore db;

    private List<TodoLayout> todoLayoutList;
    private Context context;
    public int id;

    public TodoRecyclerAdapter(List<TodoLayout> todoLayoutList, Context context, int id) {
        this.todoLayoutList = todoLayoutList;
        this.context = context;
        this.id = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.todo_layout, parent, false);

        db = FirebaseFirestore.getInstance();

        return new ViewHolder(v, this.id);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final TodoLayout todoLayout = todoLayoutList.get(position);
        holder.setIsRecyclable(false);

        Calendar c = Calendar.getInstance();

        long deadline = todoLayout.getDate().getTime();
//        long deadtime = todoLayout.getDeadtime();
//
//        if (c.get(Calendar.DATE) == deadtime && (c.getTimeInMillis() - deadline) <= 0 && (c.getTimeInMillis() - deadline) > (-1*milDay)){
//            Log.d(TAG, "onBindViewHolder: Today");
//            Log.d(TAG, "onBindViewHolder: " + (c.getTimeInMillis()-deadline));
//        }
//        if (c.get(Calendar.DATE) - deadtime == -1 && (c.getTimeInMillis() - deadline) >= (-2*milDay)){
//            Log.d(TAG, "onBindViewHolder: Tomorrow");
//            Log.d(TAG, "onBindViewHolder: " + (c.getTimeInMillis()-deadline));
//        }
//        if (c.get(Calendar.DATE) == deadtime && (c.getTimeInMillis() - deadline) <= (-1*milDay*27)) {
//            Log.d(TAG, "onBindViewHolder: bulan depan");
//            Log.d(TAG, "onBindViewHolder: " + (c.getTimeInMillis() - deadline));
//        }
//        if (c.get(Calendar.DATE) - deadtime < -1 && (c.getTimeInMillis() - deadline) <= (-1*milDay)){
//            Log.d(TAG, "onBindViewHolder: Another day");
//        }
//        if (c.getTimeInMillis() - deadline > 0){
//
//        }

        String tName = todoLayout.getTodo_name();
        holder.todoName.setText(tName);

        String tDesc = todoLayout.getDescription();
        holder.todoDesc.setText(tDesc);

        holder.viewForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog todoDialog;
                AlertDialog.Builder todoBuilder = new AlertDialog.Builder(context);
                View view = LayoutInflater.from(context).inflate(R.layout.todo_info_layout, null);

                TextView tvName = view.findViewById(R.id.tv_todo_name);
                TextView tvDesc = view.findViewById(R.id.tv_todo_desc);
                TextView tvDate = view.findViewById(R.id.tv_todo_deadline);


                String time = todoLayout.getDeadline();
                tvDate.setText(time);

                String name = todoLayout.getTodo_name();
                String desc = todoLayout.getDescription();

                tvName.setText(name);
                tvDesc.setText(desc);

                todoBuilder.setView(view);
                todoDialog = todoBuilder.create();
                todoDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoLayoutList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView todoName;
        private TextView todoDesc;
        public ConstraintLayout viewBackground, viewForeground, viewDone;
        public int id;

        public ViewHolder(View itemView, int id) {
            super(itemView);

            this.id = id;

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
