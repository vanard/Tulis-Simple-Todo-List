package com.vanard.tulis;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

public class AddTodoActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddTodoActivity";
    public static final String PREF_NAME = "ToDoFile";
    
    Toolbar todoToolbar;

    private long z;

    private long miliseconds;
    private long days;
    private long now;
    private String dId;
    private String todoName;
    private String current_user_id;
    private String priority;
    private String description;
    private Date dated;

    private String times;

    private Boolean isUpdate = false;
    private Boolean isExpired = false;
    FloatingActionButton fab;
    private Calendar c;
    private Calendar cc;

    private CalendarView datepick;
    private Spinner etPrior;
    private EditText etName, etDesc;
    private TextView tvTime;
    private LinearLayout timepick;
    private SpotsDialog dialog;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    private String catDId;
    private String catTit;

    private String loc;
    private int high;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String restore = pref.getString("category", null);
        if (restore != null) {
            catTit = pref.getString("title", "Todo");
            catDId = restore;
        }

        Intent i = getIntent();
        if (i.hasExtra("from")){
            loc = i.getStringExtra("from");
        }

        todoToolbar = findViewById(R.id.add_todo_toolbar);
        setSupportActionBar(todoToolbar);
        getSupportActionBar().setTitle("Add Todo");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bindData();

        dialog = new SpotsDialog(this);

        ArrayAdapter<CharSequence> priorAdapter = ArrayAdapter.createFromResource(this, R.array.priority, android.R.layout.simple_spinner_item);
        priorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etPrior.setAdapter(priorAdapter);
        etPrior.setOnItemSelectedListener(this);

        c = Calendar.getInstance();
        cc = Calendar.getInstance();
        z = c.get(Calendar.DATE);
        now = c.getTimeInMillis();

        listener();
    }

    private void bindData(){
        fab = findViewById(R.id.fab_add_todo);
        etName = findViewById(R.id.et_todo_name);
        etDesc = findViewById(R.id.et_desc);
        etPrior = findViewById(R.id.priority_spinner);
        tvTime = findViewById(R.id.todo_timepick);
        timepick = findViewById(R.id.timepick);
        datepick = findViewById(R.id.datepick);
    }

    public void listener(){
        timepick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "Time Picker");
            }
        });

        datepick.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                long isExp = now - c.getTimeInMillis();

                if (isExp > 0){
                    if (Build.VERSION.SDK_INT >= 23){
                        fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.description)));
                        fab.setEnabled(false);
                        isExpired = true;

                        toast("Date is expired");
                    }
                    if (Build.VERSION.SDK_INT < 23){
                        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.description)));
                        fab.setEnabled(false);
                        isExpired = true;

                        toast("Date is expired");
                    }
                }else{
                    if (Build.VERSION.SDK_INT >= 23){
                        fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.blueBtn)));
                        fab.setEnabled(true);
                        isExpired = false;
                    }
                    if (Build.VERSION.SDK_INT < 23){
                        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blueBtn)));
                        fab.setEnabled(true);
                        isExpired = false;
                    }
                }

                cc.set(Calendar.YEAR, year);
                cc.set(Calendar.MONTH, month);
                cc.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isExpired){

                    miliseconds = c.getTimeInMillis();
                    days = cc.get(Calendar.DATE);

                    String date = DateFormat.format("MMM dd, yyyy", new Date(miliseconds)).toString();
                    dated = c.getTime();

                    todoName = etName.getText().toString();
                    description = etDesc.getText().toString();

                    current_user_id = mAuth.getCurrentUser().getUid();

                    if (!isUpdate){
                        dialog.show();
                        Log.d(TAG, "onClick: masukin data");
                        setData(todoName, description, priority, date, miliseconds, days, current_user_id);
                    }else{
                        dialog.show();
                        Log.d(TAG, "onClick: update data");

                    }
                }
            }
        });
    }

    private void setData(String todo_name, String desc, String priority, String deadline, long milis, long day, String user_id){

        dId = UUID.randomUUID().toString();
        String por = high + dId;
        Log.d(TAG, "setData: " + por);

        if (!todo_name.isEmpty() && !priority.isEmpty() && !desc.isEmpty() && deadline != null && times != null){
                Map<String, Object> todoMap = new HashMap<>();
                todoMap.put("user_id", user_id);
                todoMap.put("todo_name", todo_name);
                todoMap.put("description", desc);
                todoMap.put("priority", priority);
                todoMap.put("deadline", deadline);
                todoMap.put("time", times);
                todoMap.put("milis", milis);
                todoMap.put("deadtime", day);
                todoMap.put("date", dated);
                todoMap.put("high", high);
                todoMap.put("timestamp", FieldValue.serverTimestamp());
                todoMap.put("documentId", por);
                todoMap.put("category_title", catTit);
                todoMap.put("category_documentId", catDId);

            if (loc.equals("category")){

                db.collection("Category/" + catDId + "/Todo").document(por).set(todoMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(AddTodoActivity.this, TodoActivity.class));
                                finish();

                                dialog.dismiss();
                            }
                        });
            }else{
                db.collection("Todo").document(por).set(todoMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent i = new Intent(AddTodoActivity.this, MainActivity.class);
                                i.putExtra("from", "fragment");
                                startActivity(i);
                                finish();

                                dialog.dismiss();
                            }
                        });
            }
        }else{
            toast("Fill the form first");
            dialog.dismiss();
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String am_pm = "AM";

        int hour_12_format;
        if(hourOfDay > 12){
            am_pm = "PM";
            hour_12_format = hourOfDay - 12;
        }else if (hourOfDay == 12){
            hour_12_format = hourOfDay;
            am_pm = "PM";
        }else{
            hour_12_format = hourOfDay;
        }

        times = String.format("%02d:%02d", hour_12_format, minute)+ " " + am_pm;

        tvTime.setText(times);

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        priority = parent.getItemAtPosition(position).toString();
        high = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
