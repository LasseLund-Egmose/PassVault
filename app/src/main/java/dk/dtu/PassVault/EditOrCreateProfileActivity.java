package dk.dtu.PassVault;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;


public class EditOrCreateProfileActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_add);

        Button genBtn = (Button) findViewById(R.id.generate_password);
        genBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),PasswordGeneratorActivity.class);
            startActivity(intent);
        });
    }
}
