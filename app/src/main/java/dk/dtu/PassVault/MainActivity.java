package dk.dtu.PassVault;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;

@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database.dispatch(getApplicationContext(), new Database.Transaction<Credential>() {
            @Override
            public Credential doRequest(Database db) {
                Credential c1 = new Credential("Pass1");
                Credential c2 = new Credential("Pass2");
                Credential c3 = new Credential("Pass3");

                db.setCredential(c1);
                db.setCredential(c2);
                db.setCredential(c3);

                return db.getCredential();
            }

            @Override
            public void onResult(Credential result) {
                Log.i("Main", result.toString());
            }
        });
    }
}