package com.example.facedetection;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;


public abstract class SaveFileTask extends AsyncTask<Void, Void, Void> {
    String savePath;
    Activity activity;
    View layout;

    Bitmap bitmap;

    public SaveFileTask(Activity activity, View layout) {
        this.activity = activity;
        this.layout = layout;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        layout.setDrawingCacheEnabled(true);
        bitmap = layout.getDrawingCache();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        savePath = ImageEditActivity.saveToAlbum(bitmap, activity);
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        layout.setDrawingCacheEnabled(false);
        OnSaveFile(savePath);
    }

    public abstract void OnSaveFile(String path);
}
