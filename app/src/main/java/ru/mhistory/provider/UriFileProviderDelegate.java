package ru.mhistory.provider;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import ru.mhistory.MobileHistoryApp;

public class UriFileProviderDelegate extends FilePoiProviderDelegate {

    private final Uri uri;

    public UriFileProviderDelegate(@NonNull Uri uri, @NonNull PoiFinder pointSearcher) {
        super(pointSearcher);
        this.uri = uri;
    }

    @NonNull
    @Override
    protected Reader getFileInputStreamReader() throws FileNotFoundException {
        InputStream is = MobileHistoryApp.getContext().getContentResolver().openInputStream(uri);
        if (is == null) {
            throw new IllegalStateException("Can't open stream from " + uri);
        }
        return new BufferedReader(new InputStreamReader(is));
    }

}
