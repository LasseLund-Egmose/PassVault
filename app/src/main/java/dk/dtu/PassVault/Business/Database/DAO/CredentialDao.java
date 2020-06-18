package dk.dtu.PassVault.Business.Database.DAO;

import androidx.room.*;

import dk.dtu.PassVault.Business.Database.Entities.Credential;

@Dao
public interface CredentialDao {
    @Query("SELECT * FROM credential WHERE id = 1")
    Credential get();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Credential credential);

    @Delete
    void delete(Credential credential);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void update(Credential credential);

    default void set(Credential credential) {
        long id = insert(credential);

        if (id != 1) {
            update(credential);
        }
    }
}