package dk.dtu.PassVault.Business.Database.Entities;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity
public class Setting {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    public String key;

    @NonNull
    @ColumnInfo(name = "value")
    public String value;

}
