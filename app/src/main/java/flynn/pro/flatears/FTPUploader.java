package flynn.pro.flatears;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by clackx on 08.06.16.
 */
public class FTPUploader {
    public FTPClient mFTPClient = null;
    public static final String TAG = "FTPUploader";

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
            Log.d(TAG, "Error: could not connect to host " + host + " :: " + e);
        }
        return false;
    }

    public boolean ftpDisconnect() {
        try {
            mFTPClient.logout();
            mFTPClient.disconnect();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error occurred while disconnecting from ftp server.");
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
            Log.d(TAG, "upload failed: " + e);
        }
        return status;
    }

    public static void _uploadall() {

        // :: Start timer from one second
        //thandler.postDelayed(runnable,1000);

        new Thread(new Runnable() {
            public void run() {
                boolean connectstatus = false;
                connectstatus = ftpclient.ftpConnect("10.77.5.39", "ftp", "ftp", 21);
                if (connectstatus == true) {
                    Log.d(TAG, "Connection Success");
                    // :: START UPLOAD ALL WHEN CONNECTION SUCCESS
                    boolean status = false;
                    File dir = new File(RecordService.DEFAULT_STORAGE_LOCATION);
                    String[] dlist = dir.list();

                    for (int i = 0; i < dlist.length; i++) {
                        status = ftpclient.ftpUpload(
                                Environment.getExternalStorageDirectory()
                                        + "/CallREcorder/" + dlist[i],
                                dlist[i], "/");
                        if (status == true) {
                            Log.d(TAG, "Upload file " + dlist[i] + " success :)");
                            //handler.sendEmptyMessage(2);
                        } else {
                            Log.d(TAG, "Upload file " + dlist[i] + " failed :(");
                            //handler.sendEmptyMessage(-1);
                        }
                    }

                /* status = ftpclient.ftpUpload(
                        Environment.getExternalStorageDirectory()
                                + "/CallREcorder/" + TEMP_FILENAME,
                        TEMP_FILENAME, "/", cntx);
                        */




                    //handler.sendEmptyMessage(0);
                } else {
                    Log.d(TAG, "Connection failed");
                    //handler.sendEmptyMessage(-1);
                }
            }
        }).start();
    }
}
