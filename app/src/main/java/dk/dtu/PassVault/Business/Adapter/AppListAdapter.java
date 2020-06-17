package dk.dtu.PassVault.Business.Adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dk.dtu.PassVault.Business.Database.Entities.VaultItem;
import dk.dtu.PassVault.R;

public class AppListAdapter extends ArrayAdapter<ApplicationInfo> implements SpinnerAdapter {

    protected final Context context;
    protected final ArrayList<ApplicationInfo> items;
    protected final int resourceID;

    public AppListAdapter(Context context, int resourceID, ArrayList<ApplicationInfo> items) {
        super(context, resourceID, items);

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

        ApplicationInfo item = this.items.get(position);

        CharSequence label = getContext().getPackageManager().getApplicationLabel(item);
        ((TextView) itemView.findViewById(R.id.app_list_name)).setText(label);

        return itemView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return this.getView(position, convertView, parent);
    }
}
