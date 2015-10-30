package com.ToxicBakery.app.screenshot_redaction.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;

public class PermissionCheck {

    public static boolean hasPermissions(@NonNull Context context,
                                         @NonNull String[] requiredPermissions) {

        for (String requiredPermission : requiredPermissions) {
            int grant = PermissionChecker.checkSelfPermission(context, requiredPermission);
            if (grant != PermissionChecker.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

}
