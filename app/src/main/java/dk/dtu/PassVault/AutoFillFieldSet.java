package dk.dtu.PassVault;

import android.view.autofill.AutofillId;

public class AutoFillFieldSet {

    public AutofillId username = null;
    public AutofillId password = null;

    @Override
    public String toString() {
        return "AutoFillFieldSet{" +
                "username=" + username +
                ", password=" + password +
                '}';
    }
}
