package dk.dtu.PassVault;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;

public class DatabaseDemoActivity extends BaseActivity {

    protected static class DatabaseHasCredential extends Database.Transaction<Boolean> {

        protected WeakReference<DatabaseDemoActivity> ref;

        public DatabaseHasCredential(WeakReference<DatabaseDemoActivity> ref) {
            this.ref = ref;
        }

        @Override
        public Boolean doRequest(Database db) {
            return db.hasCredential();
        }

        @Override
        public void onResult(Boolean result) {
            DatabaseDemoActivity activity = this.ref.get();

            if(activity == null) {
                return;
            }

            if(result) {
                Toast.makeText(activity.getApplicationContext(), "Vi har et master password", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity.getApplicationContext(), "Vi har ikke et master password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHasCredential transaction = new DatabaseHasCredential(new WeakReference<>(this));
        Database.dispatch(getApplicationContext(), transaction);
    }

}
