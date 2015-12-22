package com.subbu.sunshine;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;

/**
 * Created by subrahmanyam on 18-12-2015.
 */
public class SunshineApplication extends Application {
    private OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.InitializerBuilder initializerBuilder =
                Stetho.newInitializerBuilder(this);

        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(this)
        );

        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(getBaseContext())
        );

        Stetho.Initializer initializer = initializerBuilder.build();

        Stetho.initialize(initializer);
        okHttpClient = new OkHttpClient();
        okHttpClient.networkInterceptors().add(new StethoInterceptor());
//        OkHttpClient okHttpClient = new OkHttpClient();
//        okHttpClient.networkInterceptors().add(new StethoInterceptor());
//        OkHttpDownloader okHttpDownloader = new OkHttpDownloader(okHttpClient);
//        Picasso picasso = new Picasso.Builder(this).downloader(okHttpDownloader).build();
//        Picasso.setSingletonInstance(picasso);
    }
}
