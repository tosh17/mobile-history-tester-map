package ru.mhistory.fileservice;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ru.mhistory.common.util.FileUtil;
import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;

/**
 * Created by shcherbakov on 18.11.2017.
 */

public class ServerLoaderProvider {

    public void load(File file, onServerLoadFinish onServerLoadFinish) {
    }

    public interface onServerLoadFinish {
        public void loadFinished(boolean statusLoad);
    }

    public void unzip(String path, String zipname) throws IOException {
        final String LOG_TAG = "unZip";
        AsyncTask<Void, Void, Boolean> ex = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Logger.d(LogType.Load, "UnZip start ");
                InputStream is;
                ZipInputStream zis;
                try {
                    String filename;
                    is = new FileInputStream(path + zipname);
                    zis = new ZipInputStream(new BufferedInputStream(is));
                    ZipEntry ze;
                    byte[] buffer = new byte[1024];
                    int count;

                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();
                        if (ze.isDirectory()) {
                            File fmd = new File(path + filename);
                            fmd.mkdirs();
                            continue;
                        }

                        FileOutputStream fout = new FileOutputStream(path + filename);

                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }

                        fout.close();
                        zis.closeEntry();
                    }

                    zis.close();
                    Logger.d(LogType.Load, "UnZip stop ");
                    FileUtil.listDir(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        };
        ex.execute();

    }

    public boolean loadAndUnzip(String fileName) {
        return false;
    }
}
