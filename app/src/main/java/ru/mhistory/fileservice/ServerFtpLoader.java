package ru.mhistory.fileservice;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ru.mhistory.log.LogType;
import ru.mhistory.log.Logger;

/**
 * Created by shcherbakov on 18.11.2017.
 */

public class ServerFtpLoader extends ServerLoaderProvider {
    //Todo: сделать конфигуратор
    private static String SERVER = "ftp.mhistory-ru.1gb.ru";
    private static int PORT = 21;
    private static String User = "mhapp";
    private static String Pass = "okEKyNpA";
    String FILE_NAME = "34pois.zip";

    public ServerFtpLoader(String name) {
        super();
        FILE_NAME=name+".zip";
    }

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

//todo https://nikshits.blogspot.ru/2017/02/uploaddownload-file-using-ftp-server-in.html
    private Boolean downloadAndSaveFile(String server, int portNumber,
                                        String user, String password, String filename, File localFile)
            throws IOException {
        FTPClient ftp = null;

        try {
            ftp = new FTPClient();
            Logger.d(LogType.Load, "Try to connect %s",server);
            ftp.connect(server, portNumber);
            Logger.d(LogType.Load, "Connected. Reply: %s",ftp.getReplyString());

            ftp.login(user, password);
            Logger.d(LogType.Load, "Logged in");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            OutputStream outputStream = null;
            boolean success = false;
            try {
                Logger.d(LogType.Load, "Downloading");
                outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
                success = ftp.retrieveFile(filename, outputStream);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            Logger.d(LogType.Load, "Downloading done with status:"+success);
            return success;
        } finally {
            if (ftp != null) {
                Logger.d(LogType.Load, "client finish");
                ftp.logout();
                ftp.disconnect();
            }
        }
    }


}

