package com.dayosoft.tiletron.app;

import android.app.Application;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

/**
 * Created by joseph on 5/4/14.
 */
public class Tiletron extends Application {
    Permission[] permissions = new Permission[] {
            Permission.USER_PHOTOS,
            Permission.EMAIL,
            Permission.PUBLISH_STREAM,
            Permission.PUBLISH_ACTION
    };
    private SimpleFacebookConfiguration configuration;

    @Override
    public void onCreate() {
        super.onCreate();
        configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getResources().getString(R.string.app_id))
                .setNamespace("red_green_blue")
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);
    }
}
