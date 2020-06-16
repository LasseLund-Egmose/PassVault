package dk.dtu.PassVault;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();



        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();



        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),WalletActivity.class);
                startActivity(intent);
          }
        });

    }
}