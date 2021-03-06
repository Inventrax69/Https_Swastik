/***
 * Copyright (c) 2012 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inventrax.swastik.services.appupdate;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.NotificationCompat;

import com.inventrax.swastik.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleHttpDownloadStrategy implements DownloadStrategy {
    public static final Parcelable.Creator<SimpleHttpDownloadStrategy> CREATOR =
            new Parcelable.Creator<SimpleHttpDownloadStrategy>() {
                public SimpleHttpDownloadStrategy createFromParcel(Parcel in) {
                    return (new SimpleHttpDownloadStrategy());
                }

                public SimpleHttpDownloadStrategy[] newArray(int size) {
                    return (new SimpleHttpDownloadStrategy[size]);
                }
            };

    @Override
    public Uri downloadAPK(Context ctxt, String url) throws Exception {
        File apk = getDownloadFile(ctxt);

        if (apk.exists()) {
            apk.delete();
        }

        NotificationManager mNotifyManager =
                (NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(ctxt);
        mBuilder.setContentTitle("Downloading Update ...")
                .setContentText("Download in progress")
                .setSmallIcon(R.mipmap.in_launcher);
        mBuilder.setAutoCancel(false);



        HttpURLConnection conn =
                (HttpURLConnection) new URL(url).openConnection();

        try {
            conn.connect();

            int status = conn.getResponseCode();

            if (status == 200) {
                InputStream is = conn.getInputStream();
                OutputStream f = openDownloadFile(ctxt, apk);
                byte[] buffer = new byte[4096];
                int len1 = 0;

                while ((len1 = is.read(buffer)) > 0) {
                    f.write(buffer, 0, len1);
                    mBuilder.setProgress(0,0, true);
                    // Displays the progress bar for the first time.
                    mNotifyManager.notify(1, mBuilder.build());
                }

                f.close();
                is.close();
            } else {
                throw new RuntimeException(
                        String.format("Received %d from server",
                                status));
            }
        } finally {
            conn.disconnect();
            mBuilder.setContentText("Download complete")
                    // Removes the progress bar
                    .setProgress(0, 0, false);
            mNotifyManager.notify(1, mBuilder.build());
            mNotifyManager.cancel(1);

        }

        return (getDownloadUri(ctxt, apk));
    }

    @Override
    public int describeContents() {
        return (0);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // no-op
    }

    protected File getDownloadFile(Context ctxt) {
        File updateDir =
                new File(ctxt.getExternalFilesDir(null), ".CWAC-Update");

        updateDir.mkdirs();

        return (new File(updateDir, "update.apk"));
    }

    protected OutputStream openDownloadFile(Context ctxt, File apk) throws FileNotFoundException {
        return (new FileOutputStream(apk));
    }

    protected Uri getDownloadUri(Context ctxt, File apk) {
        return (Uri.fromFile(apk));
    }
}
