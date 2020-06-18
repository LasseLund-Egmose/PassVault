package dk.dtu.PassVault.Business.Database.DAO;

import androidx.room.*;

import java.util.List;

import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

@Dao
public interface VaultItemDao {

    @Query("SELECT * FROM vault_item")
    List<VaultItem> getAll();

    @Query("SELECT * FROM vault_item WHERE uri = :URI LIMIT 1")
    VaultItem getByURI(String URI);

    @Insert
    void insertAll(VaultItem... vaultItems);

    @Delete
    void delete(VaultItem vaultItem);
}
