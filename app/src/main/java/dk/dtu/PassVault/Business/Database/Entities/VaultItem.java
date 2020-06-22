package dk.dtu.PassVault.Business.Database.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vault_item")
public class VaultItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "uri")
    public String URI;

    @ColumnInfo(name = "display_name")
    public String displayName;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password")
    public byte[] password;

    public VaultItem(String URI, String displayName, String username, byte[] password) {
        this.URI = URI;
        this.displayName = displayName;
        this.username = username;
        this.password = password;
    }
}
