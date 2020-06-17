package dk.dtu.PassVault.Business.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dk.dtu.PassVault.Business.Database.Entities.VaultItem;
import dk.dtu.PassVault.R;

public class AppListAdapter extends ArrayAdapter<PackageInfo> {

    protected final Context context;
    protected final ArrayList<PackageInfo> items;
    protected final int resourceID;

    public AppListAdapter(Context context, int resourceID, ArrayList<PackageInfo> items) {
        super(context, -1, items);

        this.context = context;
        this.items = items;
        this.resourceID = resourceID;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @NonNull
    @Override
    public View getView(int position, View itemView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(itemView == null) {
            itemView = inflater.inflate(this.resourceID, null, true);
        }

        PackageInfo item = this.items.get(position);

        ((TextView) itemView.findViewById(R.id.app_list_name)).setText(item.packageName);

        return itemView;
    }
}
