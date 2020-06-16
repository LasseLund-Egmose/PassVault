package dk.dtu.PassVault.Business.Database.Entities;

import androidx.room.*;

@Entity
public class Credential {

    @PrimaryKey
    public int id = 1;

    @ColumnInfo(name = "master_password")
    public String masterPassword;

    public Credential(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    public boolean match(String input) {
        return input.equals(this.masterPassword);
    }
}
