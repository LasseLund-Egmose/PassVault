package dk.dtu.PassVault;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;


public class RegisterMasterActivty extends AppCompatActivity{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register_master);

            passwordStrengthCheck();
        }

//ADD LISTENER!!
        public void passwordStrengthCheck(){

            //changes text of strength indicator
            TextView tv = findViewById(R.id.password_strength_textView);
            tv.setText(R.string.password_strength_indicator + "Weak");

            //changes the stength indicator progressbar
            ProgressBar pb = (ProgressBar) findViewById(R.id.strength_progressbar);
            pb.setProgress(66);
            pb.setProgressDrawable(getDrawable(R.drawable.pb_drawable_yellow));
        }
    }

