package dk.dtu.PassVault.Android.Adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dk.dtu.PassVault.Business.Util.IconExtractor;
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

        Drawable appIcon = IconExtractor.extractIcon(this.context,"app://" + item.packageName);
        ImageView icon = itemView.findViewById(R.id.app_list_icon);
        icon.setImageDrawable(appIcon);

        return itemView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return this.getView(position, convertView, parent);
    }
}
