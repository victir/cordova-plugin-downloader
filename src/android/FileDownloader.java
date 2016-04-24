package org.apache.cordova.filedownloader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.os.Environment;
import android.content.pm.PackageManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.database.Cursor;

import android.content.ActivityNotFoundException;
import android.util.Log;

public class FileDownloader extends CordovaPlugin {
    private static final String FILE_DOWNLOADER = "FileDownloader";

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();

        String extension = getExtension(args, callbackContext);

        if ("startDownloading".equals(action)) {
            if (extension != null) {
              String fileURL = args.getString(0);
              this.downloadFile(context, fileURL, callbackContext);
            }
            return true;
        } else {
            return false;
        }
    }

    private String getExtension(final JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject obj = new JSONObject();
        if (args.length() > 0) {
            String url = args.getString(0);
            if (url.lastIndexOf(".") > -1) {
                String extension = url.substring(url.lastIndexOf("."));
                return extension;
            } else {
                obj.put("message", "This file :" + url + " has no extension");
                callbackContext.error(obj);
                return null;
            }
        } else {
            obj.put("message", "Parameter is missing");
            callbackContext.error(obj);
            return null;
        }
    }

    private void downloadFile(final Context context, final String fileUrl, final CallbackContext callbackContext) {
        final String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        final String extension = fileUrl.substring(fileUrl.lastIndexOf("."));
        final File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);

                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));

                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        try {
                            JSONObject obj = new JSONObject();

                          obj.put("message", "Successfull downloading");
                          callbackContext.success(obj);
                        } catch (JSONException e) {
                            Log.d(FILE_DOWNLOADER, "downloadFile", e);
                        }
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        manageDownloadStatusFailed(cursor, callbackContext);
                    }
                }
                cursor.close();
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager.enqueue(request);
    }

    private void manageDownloadStatusFailed(Cursor cursor, CallbackContext callbackContext) {
        int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
        String failedReason = "";

        switch (reason) {
            case DownloadManager.ERROR_CANNOT_RESUME:
                failedReason = "ERROR_CANNOT_RESUME";
                break;
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                failedReason = "ERROR_DEVICE_NOT_FOUND";
                break;
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                failedReason = "ERROR_FILE_ALREADY_EXISTS";
                break;
            case DownloadManager.ERROR_FILE_ERROR:
                failedReason = "ERROR_FILE_ERROR";
                break;
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                failedReason = "ERROR_HTTP_DATA_ERROR";
                break;
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                failedReason = "ERROR_INSUFFICIENT_SPACE";
                break;
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                failedReason = "ERROR_TOO_MANY_REDIRECTS";
                break;
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                break;
            case DownloadManager.ERROR_UNKNOWN:
                failedReason = "ERROR_UNKNOWN";
                break;
            case 400:
                failedReason = "BAD_REQUEST";
                break;
            case 401:
                failedReason = "UNAUTHORIZED";
                break;
            case 404:
                failedReason = "NOT_FOUND";
                break;
            case 500:
                failedReason = "INTERNAL_SERVER_ERROR";
                break;
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("message", "Download failed for reason: #" + reason + " " + failedReason);
            callbackContext.error(obj);
        } catch (JSONException e) {
            Log.d(FILE_DOWNLOADER, "downloadFile", e);
        }
    }
}
