package dk.dtu.PassVault.Business.Database;

import androidx.room.Database;

import dk.dtu.PassVault.Business.Database.DAO.CredentialDao;
import dk.dtu.PassVault.Business.Database.DAO.SettingDao;
import dk.dtu.PassVault.Business.Database.DAO.VaultItemDao;
import dk.dtu.PassVault.Business.Database.Entities.Credential;
import dk.dtu.PassVault.Business.Database.Entities.Setting;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

@Database(entities = {Credential.class, Setting.class, VaultItem.class}, version = 1, exportSchema = false)
public abstract class RoomDatabase extends androidx.room.RoomDatabase {

    public abstract CredentialDao credentialDao();
    public abstract SettingDao settingDao();
    public abstract VaultItemDao vaultItemDao();
}
