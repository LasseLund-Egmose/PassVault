package dk.dtu.PassVault;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterMasterActivty extends AppCompatActivity{
    private static final String TAG = "Log_Pass";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register_master);
            getSupportActionBar().hide();

            passwordStrengthCheck();
        }

//ADD LISTENER!!
        public void passwordStrengthCheck(){

            //changes text of strength indicator
            TextView tv = findViewById(R.id.password_strength_textView);
            tv.setText(getString(R.string.password_strength_indicator) + " Weak");


            //changes the stength indicator progressbar
            ProgressBar pb = (ProgressBar) findViewById(R.id.strength_progressbar);
            pb.setProgress(66);
            pb.setProgressDrawable(getDrawable(R.drawable.pb_drawable_yellow));
        }
    }

