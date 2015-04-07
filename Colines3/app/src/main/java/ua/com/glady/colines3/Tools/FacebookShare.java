package ua.com.glady.colines3.Tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;

/**
 * Class provides ability to share some information on Facebook.
 *
 * Created by Slava on 02.04.2015.
 */
public class FacebookShare {

    public static void shareOnFacebook(Context context, String urlToShare){

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        // Very well-known bug of facebook - it didn't catch details of the message to share
        // Only link is available. Lots of the topics on the SO and so on...
        // http://stackoverflow.com/questions/12547088/how-do-i-customize-facebooks-sharer-php
        // So by design it only share application link
        // intent.putExtra(Intent.EXTRA_SUBJECT, message); // NB: has no effect!

        intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

        // See if official Facebook app is found

        boolean facebookAppFound = false;
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                intent.setPackage(info.activityInfo.packageName);
                facebookAppFound = true;
                break;
            }
        }

        // As fallback, launch sharer.php in a browser
        if (!facebookAppFound) {
            String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
        }

        context.startActivity(intent);
    }

}
