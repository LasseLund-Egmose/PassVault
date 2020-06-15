package dk.dtu.PassVault.Business.Database.Entities;

import androidx.room.*;

@Entity
public class Credential {

    @PrimaryKey
    public int uid = 1;

    // TODO: Encrypt below
    @ColumnInfo(name = "master_password")
    public String masterPassword;

    public Credential(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    public boolean match(String input) {
        return input.equals(this.masterPassword);
    }

    @Override
    public String toString() {
        return "Credential{" +
                "uid=" + uid +
                ", masterPassword='" + masterPassword + '\'' +
                '}';
    }
}
