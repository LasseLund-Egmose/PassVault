package dk.dtu.PassVault;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class WalletActivity extends AppCompatActivity {

    public static final int ADD_PROFILE_CODE = 1;
    public static ArrayList<Button> profileButtons = new ArrayList<Button>(0);

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        getSupportActionBar().hide();


        FloatingActionButton addButton = findViewById(R.id.fabAdd);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),EditOrCreateProfileActivity.class);
                startActivityForResult(intent, ADD_PROFILE_CODE);
            }
        });



    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADD_PROFILE_CODE && resultCode == RESULT_OK) {


        }
    }
}
