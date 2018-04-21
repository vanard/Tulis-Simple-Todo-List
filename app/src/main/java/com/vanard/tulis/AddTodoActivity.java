package com.vanard.tulis;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class AddTodoActivity extends AppCompatActivity {

    Toolbar todoToolbar;

    String title_category;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);

        Intent i = getIntent();
        if (i.hasExtra("title")){
            title_category = i.getStringExtra("title");
        }

        todoToolbar = findViewById(R.id.add_todo_toolbar);
        setSupportActionBar(todoToolbar);
        getSupportActionBar().setTitle("Add Todo");

        fab = findViewById(R.id.fab_add_todo);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i = new Intent(AddTodoActivity.this, TodoActivity.class);
        i.putExtra("title", title_category);
        startActivity(i);
        finish();
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);

        View v = getActionBar().getCustomView();
        TextView tv = v.findViewById(R.id.txt_head);
        tv.setText(titleId);
    }
}
