package dk.dtu.PassVault;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;

public class DatabaseDemoActivity extends AppCompatActivity {

    protected static class CredentialTransaction extends Database.Transaction<Credential> {

        protected WeakReference<Activity> activityReference;

        CredentialTransaction(Activity ref) {
            this.activityReference = new WeakReference<>(ref);
        }

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
            // TextView tv = (TextView) activityReference.get().findViewById(R.id.hello_world_text);
            // tv.setText("Pass: " + result.masterPassword);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Database.dispatch(getApplicationContext(), new CredentialTransaction(this));
    }

}
