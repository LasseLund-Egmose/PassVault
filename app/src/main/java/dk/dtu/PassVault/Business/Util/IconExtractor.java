package dk.dtu.PassVault.Business.Util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.net.URI;

import dk.dtu.PassVault.R;

public class IconExtractor {

    protected static Drawable extractAppIcon(Context context, String pkg) {
        try {
            return context.getPackageManager().getApplicationIcon(pkg);
        } catch (PackageManager.NameNotFoundException ignored) {}

        return null;
    }

    protected static Drawable extractWebIcon(Context context, String domain) {
        // TODO: Implement fetching icon through domain
        return context.getResources().getDrawable(R.drawable.ic_baseline_web_24);
    }

    public static Drawable extractIcon(Context context, String URIString) {
        URI uri = URI.create(URIString);

        String scheme = uri.getScheme();
        if(scheme == null) {
            return null;
        }

        if(scheme.equals("app")) {
            return extractAppIcon(context, uri.getHost());
        }

        if(scheme.equals("http") || scheme.equals("https")) {
            return extractWebIcon(context, uri.getHost());
        }

        return null;
    }

}
