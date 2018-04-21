package com.vanard.tulis;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class AddCategoryActivity extends AppCompatActivity {
    private static final String TAG = "AddCategoryActivity";

    private Toolbar categoryToolbar;
    private FloatingActionButton fabAddCategory;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String current_user_id;
    private String category;
    private String dId;
    private Boolean isUpdate = false;

    private EditText etCategory;
    private SpotsDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        categoryToolbar = findViewById(R.id.todo_toolbar);
        setSupportActionBar(categoryToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Category");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dialog = new SpotsDialog(this);

        etCategory = findViewById(R.id.et_category);
        fabAddCategory = findViewById(R.id.fab_add_category);

        Intent i = getIntent();
        if (i.hasExtra("name") && i.hasExtra("dId")){
            Log.d(TAG, "onCreate: onUpdate");
            category = i.getStringExtra("name");
            dId = i.getStringExtra("dId");
            etCategory.setText(category);
            isUpdate = true;
        }

        fabAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = etCategory.getText().toString();
                current_user_id = mAuth.getCurrentUser().getUid();
                if (!isUpdate){
                    dialog.show();
                    Log.d(TAG, "onClick: masukin data");
                    setData(category, current_user_id);
                }else{
                    dialog.show();
                    Log.d(TAG, "onClick: update data");
                    updateData(category);
                }
            }
        });
    }

    private void updateData(String cat) {
        Log.d(TAG, "updateData: documentId : " + dId);
        db.collection("Category").document(dId)
                .update("category_title", cat)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toast("Updated");
                        startActivity(new Intent(AddCategoryActivity.this, MainActivity.class));
                        finish();
                        dialog.dismiss();
                    }
                });
    }

    private void setData(String cat, String user_id){

        String dId = UUID.randomUUID().toString();
        Log.d(TAG, "setData: " + dId);

        if (!cat.isEmpty()){
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("user_id", user_id);
            categoryMap.put("category_title", cat);
            categoryMap.put("timestamp", FieldValue.serverTimestamp());
            categoryMap.put("documentId", dId);

            db.collection("Category").document(dId).set(categoryMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid) {
                              toast("Category was added");
                              startActivity(new Intent(AddCategoryActivity.this, MainActivity.class));
                              finish();
                              dialog.dismiss();
                          }
                      });
        }else{
            toast("Fill the form first");
        }
    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
