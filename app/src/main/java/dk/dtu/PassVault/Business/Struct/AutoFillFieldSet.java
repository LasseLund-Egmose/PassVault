package dk.dtu.PassVault.Business.Struct;

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
