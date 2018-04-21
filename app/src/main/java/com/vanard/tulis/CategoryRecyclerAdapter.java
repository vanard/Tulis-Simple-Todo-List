package com.vanard.tulis;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {
    private static final String TAG = "CategoryRecyclerAdapter";

    private SpotsDialog dialog;

    private CategoryRecyclerAdapter adapter;
    private List<CategoryLayout> categoryList;
    private Context context;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public CategoryRecyclerAdapter(List<CategoryLayout> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_layout, parent, false);
        context = parent.getContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog(context);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryRecyclerAdapter.ViewHolder holder, final int position) {
        final CategoryLayout categoryLayout = categoryList.get(position);
        holder.setIsRecyclable(false);

        final String user_id = categoryLayout.getUser_id();

        String titleData = categoryLayout.getCategory_title();
        holder.setCategoryTitle(titleData);

        long milliseconds = categoryLayout.getTimestamp().getTime();
        String dateString = DateFormat.format("MMM dd, yyyy", new Date(milliseconds)).toString();
        holder.setCategoryDate(dateString);

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
                                final String documentId = categoryLayout.getDocumentId();
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onMenuItemClick: Deleted : " + documentId);
                                db.document("Category/" + documentId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        categoryList.remove(position);
                                        dialog.dismiss();
                                    }
                                });
                                return true;
                            case R.id.category_edit:
                                Intent i = new Intent(context, AddCategoryActivity.class);
                                i.putExtra("name", categoryLayout.getCategory_title());
                                i.putExtra("dId", categoryLayout.getDocumentId());
                                context.startActivity(i);
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
                i.putExtra("title", categoryLayout.getCategory_title());
                context.startActivity(i);
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
            category_menu = mView.findViewById(R.id.category_menu);
            cardViewCategory = mView.findViewById(R.id.cvCategory);
        }

        public void setCategoryTitle(String categoryTitle){
            category_title = mView.findViewById(R.id.category_title);
            category_title.setText(categoryTitle);
        }
        public void setCategoryDate(String date){
            category_date = mView.findViewById(R.id.category_date);
            category_date.setText(date);
        }
    }
}
