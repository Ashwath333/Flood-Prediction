package android.support.v4.net;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.media.TransportMediator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.ListPopupWindow;
import com.example.arvind.floodwarning.C0148R;

class ConnectivityManagerCompatHoneycombMR2 {
    ConnectivityManagerCompatHoneycombMR2() {
    }

    public static boolean isActiveNetworkMetered(ConnectivityManager cm) {
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return true;
        }
        switch (info.getType()) {
            case ListPopupWindow.POSITION_PROMPT_ABOVE /*0*/:
            case ListPopupWindow.INPUT_METHOD_NOT_NEEDED /*2*/:
            case DrawerLayout.LOCK_MODE_UNDEFINED /*3*/:
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
            case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                return true;
            case ListPopupWindow.POSITION_PROMPT_BELOW /*1*/:
            case C0148R.styleable.Toolbar_contentInsetLeft /*7*/:
            case C0148R.styleable.Toolbar_contentInsetStartWithNavigation /*9*/:
                return false;
            default:
                return true;
        }
    }
}
