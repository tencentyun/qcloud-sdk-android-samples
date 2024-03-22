package com.tencent.qcloud.costransferpractice.common;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class FilePathHelper {


    @Nullable
    public static String getNameFromUri(@NonNull Context context, @Nullable Uri uri) {

        if (uri == null) {
            return "";
        }

        String result = null;
        if ("content".equals(uri.getScheme())){
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static long getSizeFromUri(@NonNull Context context, @Nullable Uri uri) {

        if (uri == null) {
            return -1;
        }
        try {
            Cursor cursor =
                    context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                cursor.moveToFirst();
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static InputStream getInputStreamFromUri(Context context, Uri uri) throws IOException {

        return isVirtualFile(context, uri) ? getInputStreamForVirtualFile(context, uri) :
                getInputStreamForNormalFile(context, uri);
    }

    @Nullable
    public static String getAbsPathFromUri(Context context, Uri uri) {

        String filePath = "";

        if (DocumentsContract.isDocumentUri(context, uri)) { // 如果是 DocumentProvider 返回的 Uri

            final String docId = DocumentsContract.getDocumentId(uri);

            if (isExternalStorageDocument(uri)) { // 外部存储中的数据

                final String[] split = docId.split(":");

                if (split.length >= 2 && "primary".equalsIgnoreCase(split[0])) {
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            .concat("/").concat(split[1]);
                }

                // TODO: 2019-05-24 处理其他情况

            } else if (isDownloadsDocument(uri)) { // 下载缓存中的数据

                final String[] split = docId.split(":");


                if (split.length >= 2 && "raw".equals(split[0])) {
                    filePath = split[1];
                } else {
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));

                        filePath = getDataFromUri(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }


            } else if (isMediaDocument(uri)) { // 媒体数据

                final String[] split = docId.split(":");

                Uri contentUri = null;

                if (split.length >= 2) {

                    if ("image".equals(split[0])) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(split[0])) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(split[0])) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] { split[1] };
                    filePath = getDataFromUri(context, contentUri, selection, selectionArgs);
                }
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme()) && !"com.android.contacts".equalsIgnoreCase(uri.getAuthority())) {  // 如果是 ContentProvider Uri
            filePath = getDataFromUri(context, uri, null, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }

        return filePath != null && new File(filePath).exists() ? filePath : "";
    }

    @Nullable private static String getDataFromUri(@NonNull Context context, @Nullable Uri uri,
                                                   String selection, String[] selectionArgs) {

        if (uri == null) {
            return null;
        }
        Cursor cursor = null;
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    private static boolean isVirtualFile(Context context, Uri uri) throws IOException {

        if (!DocumentsContract.isDocumentUri(context, uri)) {
            return false;
        }
        int flags = 0;
        try {

            Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[]{DocumentsContract.Document.COLUMN_FLAGS},
                    null, null, null);

            if (cursor != null) {

                if (cursor.moveToFirst()) {
                    flags = cursor.getInt(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int flagVirtualDocument = 1 << 9;
        return (flags & flagVirtualDocument) != 0;
    }

    private static InputStream getInputStreamForVirtualFile(Context context, Uri uri)
            throws IOException {

        ContentResolver resolver = context.getContentResolver();

        String[] openableMimeTypes = resolver.getStreamTypes(uri, "*/*");

        if (openableMimeTypes == null ||
                openableMimeTypes.length < 1) {
            throw new FileNotFoundException();
        }

        AssetFileDescriptor assetFileDescriptor = resolver.openAssetFileDescriptor(uri, openableMimeTypes[0], null);
        if (assetFileDescriptor == null) {
            throw new IOException("open virtual file failed");
        }
        return assetFileDescriptor.createInputStream();
    }

    private static InputStream getInputStreamForNormalFile(Context context, Uri uri) throws IOException {

        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("open file failed");
        }

        return inputStream;
    }
}

