package com.cohen.trackfrombehind;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private TextView id_reg_birth_EDT;
    private EditText id_reg_height_EDT;
    private EditText id_reg_weight_EDT;
    private SwitchMaterial id_reg_gender_swc;
    private EditText id_reg_training_EDT;
    private ExtendedFloatingActionButton id_reg_continue_FLTBTN;
    private AppCompatImageButton id_BTN_calender;

    public static final String SP_KEY_TRAINER = "SP_KEY_TRAINER";

    private DatePickerDialog datePickerDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViews();
        initViews();
        initDatePicker();
    }



    private void findViews() {
        id_reg_birth_EDT = findViewById(R.id.id_reg_birth_EDT);
        id_reg_birth_EDT.setText(getTodaysDate());
        id_reg_height_EDT = findViewById(R.id.id_reg_height_EDT);
        id_reg_weight_EDT = findViewById(R.id.id_reg_weight_EDT);
        id_reg_gender_swc = findViewById(R.id.id_reg_gender_swc);
        id_reg_training_EDT = findViewById(R.id.id_reg_training_EDT);
        id_reg_continue_FLTBTN = findViewById(R.id.id_reg_continue_FLTBTN);
        id_BTN_calender = findViewById(R.id.id_BTN_calender);
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private void initViews() {
        id_reg_continue_FLTBTN.setOnClickListener(v -> registration());
        id_BTN_calender.setOnClickListener(v -> openDatePicker(v));
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);
                id_reg_birth_EDT.setText(date);

            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month) {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "SEC";
        return "JAN";
    }

    private void openDatePicker(View view) {
        datePickerDialog.show();
    }

    private void registration() {

        if(validInput(String.valueOf(datePickerDialog.getDatePicker().getYear()), id_reg_height_EDT.getText().toString(),
                id_reg_weight_EDT.getText().toString(), id_reg_training_EDT.getText().toString())){
            Trainer trainer = new Trainer()
                    .setBirthYear(datePickerDialog.getDatePicker().getYear())
                    .setHeight(Integer.parseInt(id_reg_height_EDT.getText().toString()))
                    .setWeight(Integer.parseInt(id_reg_weight_EDT.getText().toString()))
                    .setTrainAWeek(Integer.parseInt(id_reg_training_EDT.getText().toString()));
            if(id_reg_gender_swc.isSelected())
                trainer.setGender(Trainer.Gender.FEMALE);
            else
                trainer.setGender(Trainer.Gender.MALE);

            String json_trainer = new Gson().toJson(trainer);
            MySPV3.getInstance().putString(SP_KEY_TRAINER, json_trainer);
            permissionActivity();
        }

    }

    private boolean validInput(String birthYear, String height, String weight, String trainAWeek) {
        if(validBirthYear(birthYear) && validHeight(height) && validWeight(weight) && validTrainAWeek(trainAWeek))
            return true;
        return false;
    }

    private boolean validTrainAWeek(String trainAWeek) {
        if (validNumber(trainAWeek)){
            return true;
        }else{
            Toast.makeText(RegisterActivity.this, "Make  sure you entered number *train in a week*", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean validWeight(String weight) {
        if (validNumber(weight)){
            if(Integer.parseInt(weight) > 40)
                return true;
            else{
                Toast.makeText(RegisterActivity.this, "fix weight", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(RegisterActivity.this, "Make  sure you entered number *weight* - too tight", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean validHeight(String height) {
        if (validNumber(height)){
            if(Integer.parseInt(height) > 120)
                return true;
            else{
                Toast.makeText(RegisterActivity.this, "fix height", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(RegisterActivity.this, "Make  sure you entered number *height* - too tiny", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private boolean validBirthYear(String birthYear) {
        if (validNumber(birthYear)){
            if(Integer.parseInt(birthYear) - Calendar.getInstance().get(Calendar.YEAR) <= 100)
                return true;
            else{
                Toast.makeText(RegisterActivity.this, "fix birth year - too old", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(RegisterActivity.this, "Make  sure you entered number *height*", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean validNumber(String input){
        try
        {
            Integer.parseInt(input);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    private void permissionActivity() {
        startActivity(new Intent(this, PermissionActivity.class));
        finish();
    }
}