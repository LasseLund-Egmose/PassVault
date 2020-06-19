package dk.dtu.PassVault.Android.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dk.dtu.PassVault.Business.Database.Entities.VaultItem;
import dk.dtu.PassVault.Business.Util.IconExtractor;
import dk.dtu.PassVault.R;

public class VaultItemAdapter extends ArrayAdapter<VaultItem> {

    protected final Activity context;
    protected final ArrayList<VaultItem> items;
    protected final int resourceID;

    public VaultItemAdapter(Activity context, int resourceID, ArrayList<VaultItem> items) {
        super(context, -1, items);

        this.context = context;
        this.items = items;
        this.resourceID = resourceID;
    }

    public VaultItem getItem(int position) {
        return this.items.get(position);
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

        VaultItem item = this.items.get(position);

        ImageView icon = itemView.findViewById(R.id.vault_item_icon);
        TextView name = itemView.findViewById(R.id.vault_item_name);


        Drawable iconSrc = IconExtractor.extractIcon(this.context, item.URI);
        if(iconSrc != null) {
            icon.setImageDrawable(iconSrc);
        }

        name.setText(item.displayName);

        return itemView;
    }
}
