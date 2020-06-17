package dk.dtu.PassVault.Business.Database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import dk.dtu.PassVault.Business.Database.Entities.Credential;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

public class Database {

    public abstract static class Transaction<ResultType> extends AsyncTask<Database, Void, ResultType> {

        abstract public ResultType doRequest(Database db);
        abstract public void onResult(ResultType result);

        @Override
        protected ResultType doInBackground(Database... dbs) {
            return this.doRequest(dbs[0]);
        }

        @Override
        protected void onPostExecute(ResultType result) {
            this.onResult(result);
        }

    }

    /*
     * Static methods
     */

    protected static Database instance = null;

    protected static Database getInstance(Context context) {
        if(instance == null) {
            instance = new Database(context);
        }

        return instance;
    }

    public static <ResultType> void dispatch(Context context, Transaction<ResultType> transaction) {
        transaction.execute(getInstance(context));
    }


    /*
     * Instance methods
     */

    protected RoomDatabase roomInstance;

    protected Database(Context context) {
        this.roomInstance = Room.databaseBuilder(context, RoomDatabase.class, "pass-vault")
                .fallbackToDestructiveMigration()
                .build();
    }

    public void addVaultItem(VaultItem item) {
        this.roomInstance.vaultItemDao().insertAll(item);
    }

    public boolean hasCredential() {
        return this.roomInstance.credentialDao().get() != null;
    }

    // TODO: Fix this
    public boolean hasVaultItemWithURI(String URI) {
        return true;
    }

    public Credential getCredential() {
        return this.roomInstance.credentialDao().get();
    }

    // TODO: Do not only return first!
    public VaultItem getVaultItemByURI(String URI) {
        return this.getVaultItems()[0];
    }

    public VaultItem[] getVaultItems() {
        return this.roomInstance.vaultItemDao().getAll().toArray(new VaultItem[0]);
    }

    public void setCredential(Credential cred) {
        this.roomInstance.credentialDao().set(cred);
    }

}
