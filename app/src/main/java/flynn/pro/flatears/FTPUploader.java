package flynn.pro.flatears;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by clackx on 08.06.16.
 */
public class FTPUploader  {
    public FTPClient mFTPClient = null;
    public static final String TAG = "FTPUPLDR";

    static  FTPUploader ftpclient = new FTPUploader();

    public boolean ftpConnect(String host, String username, String password, int port) {
        try {
            mFTPClient = new FTPClient();
            // connecting to the host
            mFTPClient.connect(host, port);
            // now check the reply code, if positive mean connection success
            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                // login using username & password
                boolean status = mFTPClient.login(username, password);
                /*
                * Set File Transfer Mode
                *
                * To avoid corruption issue you must specified a correct
                * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
                * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
                * transferring text, image, and compressed files.
                */
                mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                mFTPClient.enterLocalPassiveMode();
                return status;
            }
        } catch (Exception e) {
            Log.d(TAG, "Не могу подключиться к серверу " + host);
        }
        return false;
    }

    public boolean ftpDisconnect() {
        try {
            mFTPClient.logout();
            mFTPClient.disconnect();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Отключение от сервера завершилось ошибкой.");
        }
        return false;
    }

    public boolean ftpUpload(String srcFilePath, String desFileName,
                             String desDirectory) {
        boolean status = false;
        try {
            FileInputStream srcFileStream = new FileInputStream(srcFilePath);
            // change working directory to the destination directory
            // if (ftpChangeDirectory(desDirectory)) {
            status = mFTPClient.storeFile(desFileName, srcFileStream);
            // }
            srcFileStream.close();
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Процесс выгрузки завершился ошибкой: " + e);
        }
        return status;
    }

    public static void _uploadall(final String serverip, Context context) {

        final DatabaseHelper db = DatabaseHelper.getInstance(context);
        Map< String, String > map = new HashMap< String, String >();
        map = db.getStates();

        final Map<String, String> finalMap = map;
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();

                boolean connectstatus = false;
                boolean status = false;
                String dpath = RecordService.DEFAULT_STORAGE_LOCATION;
                File dir = new File(dpath);
                String[] dlist = dir.list();
                String state;

                if (dlist == null) {
                    Log.e(TAG, "Рабочая директория не найдена");
                }
                else
                // :: if files exists
                if (dlist.length > 0) {
                    connectstatus = ftpclient.ftpConnect(serverip, "ftp", "ftp", 21);
                    if (connectstatus == true) {
                        Log.d(TAG, "Успешное подключение к FTP");
                        // :: START UPLOAD ALL WHEN CONNECTION SUCCESS
                        //for (int i = dlist.length - 1; i > 1; i--) {
                        for (int i = 1; i < dlist.length - 1; i++) {
                            state = ""+finalMap.get(dlist[i]);
                            Log.e(TAG, dlist[i] + "  ::  " + state);
                            if (!state.equals("UPLOADED")) {
                                status = ftpclient.ftpUpload(dpath + dlist[i], dlist[i], "/");
                                if (status == true) {
                                    Log.d(TAG, "Успешная выгрузка файла " + dlist[i]);
                                    state = "UPLOADED";
                                    if (i < dlist.length - 7)
                                        if ((new File(dpath, dlist[i])).delete()) {
                                            Log.d(TAG, "Успешное удаление файла " + dlist[i]);
                                            state = "DELETED";
                                        }
                                    //handler.sendEmptyMessage(2);
                                } else {
                                    Log.d(TAG, "Выгрузка файла " + dlist[i] + " не удалась");
                                    state = "FAILED";
                                    //handler.sendEmptyMessage(-1);
                                }
                                db.setState(dlist[i], state);
                            }
                            else {
                                Log.d(TAG, dlist[i] + "  УЖЕ БЫЛ ЗАГРУЖЕН");
                                //doMeta(dlist[i]);
                            }
                        }

                        //handler.sendEmptyMessage(0);
                    } else {
                        Log.d(TAG, "Подключиться не удалось");
                        //handler.sendEmptyMessage(-1);
                    }
                }
                Looper.loop();
            }
        }).start();
    }
}
