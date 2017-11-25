package ru.mhistory.providers;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ru.mhistory.log.Logger;

/**
 * Created by shcherbakov on 18.11.2017.
 */

public class ServerFtpLoader extends ServerLoaderProvider {
    private final String LOG_TAG = "FtpLoader";
   //Todo: сделать конфигуратор
    private static String SERVER = "ftp.mhistory-ru.1gb.ru";
    private static int PORT = 21;
    private static String User = "mhapp";
    private static String Pass = "okEKyNpA";
    private static String FILE_NAME = "34pois.zip";
@Override
    public void load(final File file, onServerLoadFinish isFinish) {

        AsyncTask<onServerLoadFinish, Void, Boolean> ex = new AsyncTask<onServerLoadFinish, Void, Boolean>() {
            private onServerLoadFinish onFinishLoad;
            @Override
            protected Boolean doInBackground(onServerLoadFinish... onFinishLoads) {
                boolean status=false;
                try {
                     status = downloadAndSaveFile(SERVER, PORT, User, Pass, FILE_NAME, file);
                    onFinishLoad=onFinishLoads[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return status;
            }

            @Override
            protected void onPostExecute(Boolean status) {
                onFinishLoad.loadFinished(status);
            }
        };
        ex.execute(isFinish);

    }


    private Boolean downloadAndSaveFile(String server, int portNumber,
                                        String user, String password, String filename, File localFile)
            throws IOException {
        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            Logger.i(LOG_TAG, "Try to connect " + server);
            ftp.connect(server, portNumber);
            Logger.i(LOG_TAG, "Connected. Reply: " + ftp.getReplyString());

            ftp.login(user, password);
            Logger.i(LOG_TAG, "Logged in");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            OutputStream outputStream = null;
            boolean success = false;
            try {
                Logger.i(LOG_TAG, "Downloading");
                outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
                success = ftp.retrieveFile(filename, outputStream);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            Logger.i(LOG_TAG, "Downloading done with status:"+success);
            return success;
        } finally {
            if (ftp != null) {
                Logger.i(LOG_TAG, "client finish");
                ftp.logout();
                ftp.disconnect();
            }
        }
    }


}

