package com.cohen.trackfrombehind;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

public class RegisterActivity extends AppCompatActivity {

    private EditText id_reg_birth_EDT;
    private EditText id_reg_height_EDT;
    private EditText id_reg_weight_EDT;
    private SwitchMaterial id_reg_gender_swc;
    private EditText id_reg_training_EDT;
    private ExtendedFloatingActionButton id_reg_continue_FLTBTN;

    public static final String SP_KEY_TRAINER = "SP_KEY_TRAINER";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViews();
        initViews();
    }

    private void findViews() {
        id_reg_birth_EDT = findViewById(R.id.id_reg_birth_EDT);
        id_reg_height_EDT = findViewById(R.id.id_reg_height_EDT);
        id_reg_weight_EDT = findViewById(R.id.id_reg_weight_EDT);
        id_reg_gender_swc = findViewById(R.id.id_reg_gender_swc);
        id_reg_training_EDT = findViewById(R.id.id_reg_training_EDT);
        id_reg_continue_FLTBTN = findViewById(R.id.id_reg_continue_FLTBTN);
    }
    private void initViews() {
        id_reg_continue_FLTBTN.setOnClickListener(v -> registration());
    }

    private void registration() {
        if(validInput()){
            Trainer trainer = new Trainer()
                    .setBirthYear(Integer.parseInt(id_reg_birth_EDT.getText().toString()))
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

    private boolean validInput() {
        return true;
    }

    private void permissionActivity() {
        startActivity(new Intent(this, PermissionActivity.class));
        finish();
    }
}