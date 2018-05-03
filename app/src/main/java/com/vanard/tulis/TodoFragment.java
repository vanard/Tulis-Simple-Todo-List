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

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodoFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
    private static final String TAG = "TodoFragment";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Query query;
    private String current_user_id;

    private TodoRecyclerAdapter adapter;
    private ArrayList<TodoLayout> todoLayoutList;

    private FloatingActionButton fabAddTodo;
    private RecyclerView rcv;
    private SpotsDialog dialog;

    public TodoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_todo, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rcv = v.findViewById(R.id.todo_rcv);
        fabAddTodo = v.findViewById(R.id.fab_todo_add);

        todoLayoutList = new ArrayList<>();

        rcv.setHasFixedSize(true);
        rcv.setLayoutManager(new LinearLayoutManager(container.getContext()));
        rcv.setItemAnimator(new DefaultItemAnimator());
        rcv.addItemDecoration(new DividerItemDecoration(container.getContext(), DividerItemDecoration.VERTICAL));

        adapter = new TodoRecyclerAdapter(todoLayoutList, getContext());
        rcv.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rcv);

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
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        TodoLayout todoLayout = doc.getDocument().toObject(TodoLayout.class);

                        todoLayoutList.add(todoLayout);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof TodoRecyclerAdapter.ViewHolder) {
            // remove the item from recycler view
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
        }
    }

}
