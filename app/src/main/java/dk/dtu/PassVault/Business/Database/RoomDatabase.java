package dk.dtu.PassVault.Business.Database;

import androidx.room.Database;

import dk.dtu.PassVault.Business.Database.DAO.CredentialDao;
import dk.dtu.PassVault.Business.Database.Entities.Credential;

@Database(entities = {Credential.class}, version = 1)
public abstract class RoomDatabase extends androidx.room.RoomDatabase {

    public abstract CredentialDao credentialDao();
}
