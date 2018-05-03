package com.vanard.tulis;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodoFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
    private static final String TAG = "TodoFragment";
    private static final long milDay = 86400000;

    View v;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Query query;
    private String current_user_id;

    private ArrayList<TodoLayout> todoLayoutList;
    private ArrayList<TodoLayout> todoTomorrow;
    private ArrayList<TodoLayout> todoOtherDay;
    private TodoRecyclerAdapter adapter;
    private TodoRecyclerAdapter adapter1;
    private TodoRecyclerAdapter adapter2;

    private FloatingActionButton fabAddTodo;
    private RecyclerView rcv_today;
    private RecyclerView rcv_tomorrow;
    private RecyclerView rcv_other;
    private TextView today, tomorrow, aday;

    public TodoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_todo, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        todoLayoutList = new ArrayList<>();
        todoTomorrow = new ArrayList<>();
        todoOtherDay = new ArrayList<>();

        settingData(container);

        fabAddTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddTodoActivity.class);
                i.putExtra("from", "fragment");
                startActivity(i);
            }
        });

        initData();

        return v;
    }

    private void initData() {
        Log.d(TAG, "initData: Load data");
        current_user_id = mAuth.getCurrentUser().getUid();

        query = db.collection("Todo").whereEqualTo("user_id", current_user_id).orderBy("date").orderBy("high");
        query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                todoLayoutList.clear();
                todoTomorrow.clear();
                todoOtherDay.clear();

                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        TodoLayout todoLayout = doc.getDocument().toObject(TodoLayout.class);

                        String docu = todoLayout.getDocumentId();

                        Calendar c = Calendar.getInstance();

                        long deadline = todoLayout.getDate().getTime();
                        long deadtime = todoLayout.getDeadtime();

                        Log.d(TAG, "onEvent: " + (c.getTimeInMillis() - deadline));
                        Log.d(TAG, "onEvent: " + (c.get(Calendar.DATE)+deadtime));

                        if (c.get(Calendar.DATE) == deadtime && (c.getTimeInMillis() - deadline) <= 0 && (c.getTimeInMillis() - deadline) > (-1*milDay)){
                            Log.d(TAG, "onEvent: hari ini");
                            todoLayoutList.add(todoLayout);
                            if (todoLayoutList.size() > 0)
                                today.setVisibility(View.VISIBLE);

                            adapter.notifyDataSetChanged();
                        }
                        if (c.get(Calendar.DATE) - deadtime == -1 && (c.getTimeInMillis() - deadline) >= (-2*milDay)){
                            Log.d(TAG, "onEvent: besok");
                            todoTomorrow.add(todoLayout);
                            if(todoTomorrow.size() > 0)
                                tomorrow.setVisibility(View.VISIBLE);
                            adapter1.notifyDataSetChanged();
                        }
                        if (c.get(Calendar.DATE) - deadtime < -1 && (c.getTimeInMillis() - deadline) <= (-1*milDay)){
                            Log.d(TAG, "onEvent: other");
                            todoOtherDay.add(todoLayout);
                            if (todoOtherDay.size() > 0)
                                aday.setVisibility(View.VISIBLE);
                            adapter2.notifyDataSetChanged();
                        }if (c.getTimeInMillis() - deadline > 0){
                            Log.d(TAG, "onEvent: terlewat");
                            db.document("Todo/"+ docu).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        TodoRecyclerAdapter.ViewHolder a = (TodoRecyclerAdapter.ViewHolder) viewHolder;
        int s = a.id;
        if (viewHolder instanceof TodoRecyclerAdapter.ViewHolder) {
            if (s == 1){
                String doc = todoLayoutList.get(position).getDocumentId();
                db.document("Todo/"+ doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
                adapter.removeItem(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(position);
                adapter.notifyDataSetChanged();
                initData();
                Log.d(TAG, "onSwiped: today hapus");
            }
            if (s == 2){
                String doc = todoTomorrow.get(position).getDocumentId();
                db.document("Todo/"+ doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
                adapter1.removeItem(viewHolder.getAdapterPosition());
                adapter1.notifyItemRemoved(position);
                adapter1.notifyDataSetChanged();
                initData();
                Log.d(TAG, "onSwiped: tomorrow hapus");
            }if (s == 3){
                String doc = todoOtherDay.get(position).getDocumentId();
                db.document("Todo/"+ doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
                adapter2.removeItem(viewHolder.getAdapterPosition());
                adapter2.notifyItemRemoved(position);
                adapter2.notifyDataSetChanged();
                initData();
                Log.d(TAG, "onSwiped: other day hapus");
            }
        }
    }

    private void settingData(ViewGroup container){
        rcv_today = v.findViewById(R.id.todo_rcv_today);
        rcv_tomorrow = v.findViewById(R.id.todo_rcv_tomorrow);
        rcv_other = v.findViewById(R.id.todo_rcv_other);
        fabAddTodo = v.findViewById(R.id.fab_todo_add);
        today = v.findViewById(R.id.todo_tv_today);
        tomorrow = v.findViewById(R.id.todo_tv_tomorrow);
        aday = v.findViewById(R.id.todo_tv_other_day);

        rcv_today.setHasFixedSize(true);
        rcv_today.setLayoutManager(new LinearLayoutManager(container.getContext()));
        rcv_today.setItemAnimator(new DefaultItemAnimator());
        rcv_today.addItemDecoration(new DividerItemDecoration(container.getContext(), DividerItemDecoration.VERTICAL));

        adapter = new TodoRecyclerAdapter(todoLayoutList, container.getContext(),1);
        rcv_today.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,this );
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rcv_today);

        rcv_tomorrow.setHasFixedSize(true);
        rcv_tomorrow.setLayoutManager(new LinearLayoutManager(container.getContext()));
        rcv_tomorrow.setItemAnimator(new DefaultItemAnimator());
        rcv_tomorrow.addItemDecoration(new DividerItemDecoration(container.getContext(), DividerItemDecoration.VERTICAL));

        adapter1 = new TodoRecyclerAdapter(todoTomorrow, container.getContext(), 2);
        rcv_tomorrow.setAdapter(adapter1);

        ItemTouchHelper.SimpleCallback itemTomorrow = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,this );
        new ItemTouchHelper(itemTomorrow).attachToRecyclerView(rcv_tomorrow);

        rcv_other.setHasFixedSize(true);
        rcv_other.setLayoutManager(new LinearLayoutManager(container.getContext()));
        rcv_other.setItemAnimator(new DefaultItemAnimator());
        rcv_other.addItemDecoration(new DividerItemDecoration(container.getContext(), DividerItemDecoration.VERTICAL));

        adapter2 = new TodoRecyclerAdapter(todoOtherDay, container.getContext(), 3);
        rcv_other.setAdapter(adapter2);

        ItemTouchHelper.SimpleCallback itemOtherDay = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,this );
        new ItemTouchHelper(itemOtherDay).attachToRecyclerView(rcv_other);
    }

}
