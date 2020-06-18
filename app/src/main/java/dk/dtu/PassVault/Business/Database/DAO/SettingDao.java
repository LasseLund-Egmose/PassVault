package dk.dtu.PassVault.Business.Database.DAO;

import androidx.room.*;

@Dao
public interface SettingDao {

    @Query("SELECT value FROM setting WHERE key = :key")
    String get(String key);

    default String getOrDefault(String key, String def) {
        String val = this.get(key);

        return val == null ? def : val;
    }

}
