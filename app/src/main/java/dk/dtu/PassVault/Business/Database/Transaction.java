package dk.dtu.PassVault.Business.Database;

import android.os.AsyncTask;

public abstract class Transaction<ResultType> extends AsyncTask<RoomDatabase, Void, ResultType> {

    abstract public ResultType doRequest(RoomDatabase db);
    abstract public void onResult(ResultType result);

    @Override
    protected ResultType doInBackground(RoomDatabase... dbs) {
        return this.doRequest(dbs[0]);
    }

    @Override
    protected void onPostExecute(ResultType result) {
        this.onResult(result);
    }

};