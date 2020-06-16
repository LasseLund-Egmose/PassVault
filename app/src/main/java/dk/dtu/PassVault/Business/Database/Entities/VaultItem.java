package dk.dtu.PassVault.Business.Database.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vault_item")
public class VaultItem {

    @PrimaryKey(autoGenerate = true)
    public int id = 1;

    @ColumnInfo(name = "platform_identifier")
    public String platformIdentifier;

    @ColumnInfo(name = "platform_display_name")
    public String platformDisplayName;

    @ColumnInfo(name = "password")
    public String password;

}
