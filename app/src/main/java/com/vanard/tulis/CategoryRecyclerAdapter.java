package com.vanard.tulis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dmax.dialog.SpotsDialog;

import static android.content.Context.MODE_PRIVATE;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {
    private static final String TAG = "CategoryRecyclerAdapter";
    public static final String PREF_NAME = "ToDoFile";
    private static final long milDay = 86400000;

    private SpotsDialog dialog;

    private CategoryRecyclerAdapter adapter;
    private List<CategoryLayout> categoryList;
    private Context context;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String currentUser;

    public CategoryRecyclerAdapter(List<CategoryLayout> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_layout, parent, false);
        context = parent.getContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog(context);
        currentUser = mAuth.getCurrentUser().getUid();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryRecyclerAdapter.ViewHolder holder, final int position) {
        final CategoryLayout categoryLayout = categoryList.get(position);
        holder.setIsRecyclable(false);

        final String document = categoryLayout.getDocumentId();
        db.collection("Category/"+document+"/Todo").whereEqualTo("user_id", currentUser).orderBy("date", Query.Direction.ASCENDING).limit(1)
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    TodoLayout todoLayout = doc.getDocument().toObject(TodoLayout.class);
                    long det = todoLayout.getMilis();
                    long deadline = todoLayout.getDate().getTime();
                    long deadtime = todoLayout.getDeadtime();

                    Calendar c = Calendar.getInstance();
                    if (c.get(Calendar.DATE) == deadtime && (c.getTimeInMillis() - deadline) <= 0 && (c.getTimeInMillis() - deadline) > (-1*milDay)) {
                        holder.category_date.setVisibility(View.VISIBLE);
                        holder.category_date.setText("Today");
                    }else{
                        try{
                            String dateString = DateFormat.format("MMM dd, yyyy", new Date(det)).toString();
                            holder.category_date.setVisibility(View.VISIBLE);
                            holder.setCategoryDate(dateString);
                        }catch (Exception ex){
                            Toast.makeText(context, "Exception : " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        final String titleData = categoryLayout.getCategory_title();
        holder.setCategoryTitle(titleData);

//        try{
//            long milliseconds = categoryLayout.getTimestamp().getTime();
//            String dateString = DateFormat.format("MMM dd, yyyy", new Date(milliseconds)).toString();
//            holder.setCategoryDate(dateString);
//        }catch (Exception e){
//            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }

        holder.category_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.category_menu);
                popupMenu.inflate(R.menu.category_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.category_delete:
                                dialog.show();
                                db.document("Category/" + document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        categoryList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemChanged(position);
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return true;
                            case R.id.category_edit:
                                final AlertDialog categoryDialog;
                                final SpotsDialog loading;
                                AlertDialog.Builder categoryBuilder = new AlertDialog.Builder(context);
                                View view = LayoutInflater.from(context).inflate(R.layout.activity_add_category, null);

                                final EditText title = view.findViewById(R.id.et_category);
                                FloatingActionButton fabAddCategory = view.findViewById(R.id.fab_add_category);

                                loading = new SpotsDialog(context);

                                String judul = categoryLayout.getCategory_title();
                                title.setText(judul);

                                categoryBuilder.setView(view);
                                categoryDialog = categoryBuilder.create();
                                categoryDialog.show();

                                fabAddCategory.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        loading.show();
                                        Log.d(TAG, "onClick: update data");
                                        final String newTit = title.getText().toString();
                                        db.collection("Category").document(document)
                                                .update("category_title", newTit)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
                                                        loading.dismiss();
                                                        categoryDialog.dismiss();
                                                        holder.category_title.setText(newTit);
                                                    }
                                                });
                                    }
                                });

                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        holder.cardViewCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TodoActivity.class);
//                i.putExtra("title", categoryLayout.getCategory_title());
//                i.putExtra("category", categoryLayout.getDocumentId());

                SharedPreferences.Editor clDoc = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                clDoc.putString("category", categoryLayout.getDocumentId());
                clDoc.putString("title", categoryLayout.getCategory_title());
                clDoc.apply();
                context.startActivity(i);
                Log.d(TAG, "onClick: " + categoryLayout.getDocumentId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private CardView cardViewCategory;
        private TextView category_title, category_date, category_menu;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            category_date = mView.findViewById(R.id.category_date);
            category_menu = mView.findViewById(R.id.category_menu);
            cardViewCategory = mView.findViewById(R.id.cvCategory);
        }

        public void setCategoryTitle(String categoryTitle){
            category_title = mView.findViewById(R.id.category_title);
            category_title.setText(categoryTitle);
        }
        public void setCategoryDate(String date){
            category_date.setText(date);
        }
    }
}
