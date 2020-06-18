package dk.dtu.PassVault.Business.Database.Entities;

import androidx.room.*;

@Entity
public class Setting {

    @PrimaryKey
    public int id = 1;

    @ColumnInfo(name = "key")
    public String key;

    @ColumnInfo(name = "value")
    public String value;

}
