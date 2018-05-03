package com.vanard.tulis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class TodoActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String TAG = "TodoActivity";
    public static final String PREF_NAME = "ToDoFile";
    private static final long milDay = 86400000;

    private Toolbar todoToolbar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Query query;

    private FloatingActionButton fabAddTodo;
    private RecyclerView rcv_today;
    private RecyclerView rcv_tomorrow;
    private RecyclerView rcv_other;
    private TextView today, tomorrow, aday;

    private String title_category;
    private String dId;
    private String current_user_id;

    private ArrayList<TodoLayout> todoLayoutList;
    private ArrayList<TodoLayout> todoTomorrow;
    private ArrayList<TodoLayout> todoOtherDay;
    private TodoRecyclerAdapter adapter;

    public String catDId;
    public String catTit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        todoToolbar = findViewById(R.id.todo_toolbar);
        setSupportActionBar(todoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String restore = pref.getString("category", null);
        if (restore != null) {
            catTit = pref.getString("title", "Todo");
            catDId = restore;
            getSupportActionBar().setTitle(catTit);
            Log.d(TAG, "onCreate: " + catDId);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        todoLayoutList = new ArrayList<>();
        todoTomorrow = new ArrayList<>();
        todoOtherDay = new ArrayList<>();

        settingData();

        getDataCategory();

        fabAddTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TodoActivity.this, AddTodoActivity.class);
                i.putExtra("title", title_category);
                i.putExtra("category", dId);
                i.putExtra("from", "category");
                startActivity(i);
            }
        });

        initData();
    }

    private void getDataCategory(){
        DocumentReference d = db.document("Category/" + dId);
        d.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    CategoryLayout cl = documentSnapshot.toObject(CategoryLayout.class);

                    catTit = cl.getCategory_title();
                    catDId = cl.getDocumentId();
                }
            }
        });
    }

    private void initData() {
        Log.d(TAG, "initData: Load data");
        current_user_id = mAuth.getCurrentUser().getUid();

        Log.d(TAG, "initData: " + catDId);

        query = db.collection("Category/" + catDId + "/Todo").whereEqualTo("user_id", current_user_id).orderBy("date").orderBy("high");
        query.addSnapshotListener(TodoActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                todoLayoutList.clear();
                todoTomorrow.clear();
                todoOtherDay.clear();
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        TodoLayout todoLayout = doc.getDocument().toObject(TodoLayout.class);

                        Calendar c = Calendar.getInstance();

                        long deadline = todoLayout.getDate().getTime();
                        long deadtime = todoLayout.getDeadtime();

                        if (c.get(Calendar.DATE) == deadtime && (c.getTimeInMillis() - deadline) <= 0 && (c.getTimeInMillis() - deadline) > (-1*milDay)){
                            todoLayoutList.add(todoLayout);
                            if (todoLayoutList.size() > 0)
                                today.setVisibility(View.VISIBLE);

                            adapter.notifyDataSetChanged();
                            return;
                        }
                        if (c.get(Calendar.DATE) - deadtime == -1 && (c.getTimeInMillis() - deadline) <= (-1*milDay)  && (c.getTimeInMillis() - deadline) >= (-2*milDay)){
                            todoTomorrow.add(todoLayout);
                            if(todoTomorrow.size() > 0)
                                tomorrow.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            return;
                        }
                        if (c.get(Calendar.DATE) - deadtime < -1 && (c.getTimeInMillis() - deadline) <= (-1*milDay)){
                            todoOtherDay.add(todoLayout);
                            if (todoOtherDay.size() > 0)
                                aday.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            return;
                        }

//                        todoLayoutList.add(todoLayout);

                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            startActivity(new Intent(TodoActivity.this, LoginActivity.class));
        }else{
            initData();
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof TodoRecyclerAdapter.ViewHolder) {
            // remove the item from recycler view
            String doc = todoLayoutList.get(position).getDocumentId();
            String catDoc = todoLayoutList.get(position).getCategory_documentId();
            db.document("Category/" + catDoc + "/Todo/"+ doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void settingData(){
        rcv_today = findViewById(R.id.todo_rcv_today);
        rcv_tomorrow = findViewById(R.id.todo_rcv_tomorrow);
        rcv_other = findViewById(R.id.todo_rcv_other);
        fabAddTodo = findViewById(R.id.fab_todo_add);
        today = findViewById(R.id.todo_tv_today);
        tomorrow = findViewById(R.id.todo_tv_tomorrow);
        aday = findViewById(R.id.todo_tv_other_day);

        rcv_today.setHasFixedSize(true);
        rcv_today.setLayoutManager(new LinearLayoutManager(this));
        rcv_today.setItemAnimator(new DefaultItemAnimator());
        rcv_today.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new TodoRecyclerAdapter(todoLayoutList, this);
        rcv_today.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rcv_today);

        rcv_tomorrow.setHasFixedSize(true);
        rcv_tomorrow.setLayoutManager(new LinearLayoutManager(this));
        rcv_tomorrow.setItemAnimator(new DefaultItemAnimator());
        rcv_tomorrow.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new TodoRecyclerAdapter(todoTomorrow, this);
        rcv_tomorrow.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTomorrow = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTomorrow).attachToRecyclerView(rcv_tomorrow);

        rcv_other.setHasFixedSize(true);
        rcv_other.setLayoutManager(new LinearLayoutManager(this));
        rcv_other.setItemAnimator(new DefaultItemAnimator());
        rcv_other.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new TodoRecyclerAdapter(todoOtherDay, this);
        rcv_other.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemOther = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemOther).attachToRecyclerView(rcv_other);
    }
}
