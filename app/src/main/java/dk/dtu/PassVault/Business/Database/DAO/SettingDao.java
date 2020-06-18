package dk.dtu.PassVault.Business.Database.DAO;

import androidx.room.*;

import dk.dtu.PassVault.Business.Database.Entities.Setting;

@Dao
public interface SettingDao {

    @Query("SELECT value FROM setting WHERE key = :key")
    String get(String key);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Setting setting);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void update(Setting setting);

    default void set(String key, String value) {
        Setting setting = new Setting();
        setting.key = key;
        setting.value = value;

        if(this.get(key) != null) {
            this.update(setting);
        } else {
            this.insert(setting);
        }
    }

}
