package com.vanard.tulis;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Query query;

    private String current_user_id;

    private FloatingActionButton fabAdd;
    private RecyclerView rcView;

    private CategoryRecyclerAdapter adapter;
    private ArrayList<CategoryLayout> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mainToolbar = findViewById(R.id.todo_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Tulis : Simple Todo List");

        categoryList = new ArrayList<>();
        rcView = findViewById(R.id.rc_view);
        fabAdd = findViewById(R.id.fab_category_add);

        rcView.setHasFixedSize(true);
        rcView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Log.d(TAG, "onCreate: user ada");
            adapter = new CategoryRecyclerAdapter(categoryList);
            rcView.setAdapter(adapter);
        }

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddCategoryActivity.class));
            }
        });
    }

    private void initData() {
        categoryList.clear();
        Log.d(TAG, "initData: Load data");
        current_user_id = mAuth.getCurrentUser().getUid();

        query = db.collection("Category").whereEqualTo("user_id", current_user_id).orderBy("timestamp", Query.Direction.DESCENDING);
        query.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            CategoryLayout categoryLayout = doc.getDocument().toObject(CategoryLayout.class);

                            categoryList.add(categoryLayout);
                            adapter.notifyDataSetChanged();
                        }
                    }
            }
        });
    }

    private void deleteItem(int order){
        db.collection("Category")
                .document(categoryList.get(order).getDocumentId())
        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Category Deleted", Toast.LENGTH_SHORT).show();
                initData();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }else{
            categoryList.clear();
            initData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout_btn:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals("Edit")){
            CategoryLayout cl = new CategoryLayout();
            String title = cl.getCategory_title();

            Intent i = new Intent(MainActivity.this, AddCategoryActivity.class);
            i.putExtra("name", title);
            startActivity(i);
        }else if(item.getTitle().equals("Delete")){
            deleteItem(item.getOrder());
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);

        View v = getActionBar().getCustomView();
        TextView tv = v.findViewById(R.id.txt_head);
        tv.setText(titleId);
    }
}
