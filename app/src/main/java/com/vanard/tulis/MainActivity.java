package com.vanard.tulis;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar todoToolbar;

    private FirebaseAuth mAuth;

    private ViewPager mMainPager;
    private TextView mCategoryLabel;
    private TextView mTodoLabel;
    private MainPagerAdapter mPagerAdapter;

    private String loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoToolbar = findViewById(R.id.add_todo_toolbar);
        setSupportActionBar(todoToolbar);
        getSupportActionBar().setTitle("Tulis");

        mAuth = FirebaseAuth.getInstance();
        Intent i = getIntent();

        mCategoryLabel = findViewById(R.id.label_category);
        mTodoLabel = findViewById(R.id.label_todo);

        mMainPager = findViewById(R.id.main_pager);
        mMainPager.setOffscreenPageLimit(1);
        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mMainPager.setAdapter(mPagerAdapter);

        mCategoryLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPager.setCurrentItem(0);
            }
        });

        mTodoLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPager.setCurrentItem(1);
            }
        });

        mMainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (i.hasExtra("from")){
            loc = i.getStringExtra("from");
            if (loc.equals("fragment")){
                mMainPager.setCurrentItem(1);
            }
        }
    }

    private void changeTabs(int position) {
        if (position == 0){
            mCategoryLabel.setTextColor(getColor(R.color.whiteGrey));
            mCategoryLabel.setBackgroundColor(getColor(R.color.holo_blue_bright));
            mCategoryLabel.setTextSize(18);

            mTodoLabel.setTextColor(getColor(R.color.holo_blue_dark));
            mTodoLabel.setBackgroundColor(getColor(R.color.whiteGrey));
            mTodoLabel.setTextSize(16);
        }
        if (position == 1){
            mTodoLabel.setTextColor(getColor(R.color.whiteGrey));
            mTodoLabel.setBackgroundColor(getColor(R.color.holo_blue_bright));
            mTodoLabel.setTextSize(18);

            mCategoryLabel.setTextColor(getColor(R.color.holo_blue_dark));
            mCategoryLabel.setBackgroundColor(getColor(R.color.whiteGrey));
            mCategoryLabel.setTextSize(16);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
            case R.id.action_sync_btn:
                return true;
            default:
                return false;
        }
    }
}
