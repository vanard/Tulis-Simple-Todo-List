package com.vanard.tulis;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment {
    private static final String TAG = "CategoryFragment";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Query query;

    private String current_user_id;
    private String docId;
    private String title;
    private Boolean isUpdate = false;

    private FloatingActionButton fabAdd;
    private RecyclerView rcView;
    private SpotsDialog dialog;
    AlertDialog categoryDialog;

    private CategoryRecyclerAdapter adapter;
    private ArrayList<CategoryLayout> categoryList;
    private View view;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_category, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog(getActivity());

        categoryList = new ArrayList<>();
        rcView = v.findViewById(R.id.rc_view);
        fabAdd = v.findViewById(R.id.fab_category_add);

        rcView.setHasFixedSize(true);
        rcView.setLayoutManager(new GridLayoutManager(container.getContext(), 2));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Log.d(TAG, "onCreate: user ada");
            adapter = new CategoryRecyclerAdapter(categoryList, getContext());
            rcView.setAdapter(adapter);
        }

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, AddCategoryActivity.class));
                categoryBuilder();
            }
        });

        initData();

        return v;
    }

    private void categoryBuilder(){

        AlertDialog.Builder categoryBuilder = new AlertDialog.Builder(getActivity());
        view = getLayoutInflater().inflate(R.layout.activity_add_category, null);

        addCategory();

        categoryBuilder.setView(view);
        categoryDialog = categoryBuilder.create();
        categoryDialog.show();
    }

    private void addCategory(){
        final EditText etCategory = view.findViewById(R.id.et_category);
        FloatingActionButton fabAddCategory = view.findViewById(R.id.fab_add_category);

        if (isUpdate){
            etCategory.setText(title);
        }

        fabAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = etCategory.getText().toString();
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

    private void initData() {
        categoryList.clear();
        current_user_id = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "initData: Load data : " + current_user_id);

        query = db.collection("Category").whereEqualTo("user_id", current_user_id).orderBy("timestamp", Query.Direction.DESCENDING);
        query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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
                toast("Category Deleted");
                initData();
            }
        });
    }

    private void updateData(String cat) {
        Log.d(TAG, "updateData: documentId : " + docId);
        db.collection("Category").document(docId)
                .update("category_title", cat)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toast("Updated");
                        dialog.dismiss();
                        isUpdate = false;
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
                            dialog.dismiss();
                            categoryDialog.dismiss();
                            initData();
                        }
                    });
        }else{
            toast("Fill the form first");
            dialog.dismiss();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals("Edit")){
            CategoryLayout cl = new CategoryLayout();
            title = cl.getCategory_title();
            docId = cl.getDocumentId();
            Log.d(TAG, "onContextItemSelected: " + docId);
            isUpdate = true;

//            Intent i = new Intent(MainActivity.this, AddCategoryActivity.class);
//            i.putExtra("name", title);
//            startActivity(i);

            categoryBuilder();

        }else if(item.getTitle().equals("Delete")){
            deleteItem(item.getOrder());
        }
        return super.onContextItemSelected(item);
    }

    private void toast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

}
