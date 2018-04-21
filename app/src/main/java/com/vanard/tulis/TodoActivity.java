package com.vanard.tulis;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;


public class TodoActivity extends AppCompatActivity {

    private Toolbar todoToolbar;

    private FloatingActionButton fabAddTodo;

    private String title_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        Intent i = getIntent();
        if (i.hasExtra("title")) {
            title_category = i.getStringExtra("title");
        }

        todoToolbar = findViewById(R.id.todo_toolbar);
        setSupportActionBar(todoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title_category);

        fabAddTodo = findViewById(R.id.fab_todo_add);

        fabAddTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TodoActivity.this, AddTodoActivity.class);
                i.putExtra("title", title_category);
                startActivity(i);
                finish();
            }
        });
    }
}
