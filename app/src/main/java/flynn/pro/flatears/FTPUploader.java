package flynn.pro.flatears;

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
                boolean status = false;
                String dpath = RecordService.DEFAULT_STORAGE_LOCATION;
                File dir = new File(dpath);
                String[] dlist = dir.list();

                // :: if files exists
                if (dlist.length > 0) {
                    connectstatus = ftpclient.ftpConnect("10.34.200.118", "ftp", "ftp", 21);
                    if (connectstatus == true) {
                        Log.d(TAG, "Connection Success");
                        // :: START UPLOAD ALL WHEN CONNECTION SUCCESS


                        for (int i = 0; i < dlist.length; i++) {
                            status = ftpclient.ftpUpload(dpath + dlist[i], dlist[i], "/");
                            if (status == true) {
                                Log.d(TAG, "Upload file " + dlist[i] + " success :)");
                                boolean deleted = (new File(dpath, dlist[i])).delete();
                                //handler.sendEmptyMessage(2);
                            } else {
                                Log.d(TAG, "Upload file " + dlist[i] + " failed :(");
                                //handler.sendEmptyMessage(-1);
                            }
                        }
                        //handler.sendEmptyMessage(0);
                    } else {
                        Log.d(TAG, "Connection failed");
                        //handler.sendEmptyMessage(-1);
                    }
                }
            }
        }).start();
    }
}
