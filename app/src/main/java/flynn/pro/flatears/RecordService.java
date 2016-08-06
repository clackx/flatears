package flynn.pro.flatears;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * Created by clackx on 08.06.16.
 */
public class RecordService 
    extends Service
    implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener
{
    private static final String TAG = "RECORDSRVC";

    public static final String FLATEARS_FOLDER_LOCATION = "/FLATEARS/";
    public static final String DEFAULT_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getPath() + FLATEARS_FOLDER_LOCATION;

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 11025;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    public static final int NOTIFICATION_ID = 1;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    String incoming;
    String rowID;

    ContentValues cv = new ContentValues();


    /*
    private static void test() throws java.security.NoSuchAlgorithmException
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();
        Key publicKey = kp.getPublic();
        Key privateKey = kp.getPrivate();
    }
    */

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate created MediaRecorder object");
        updateNotification ("IDLE", "");
    }

    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "RecordService::onStart calling through to onStartCommand");
        onStartCommand(intent, 0, startId);
        }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "RecordService::onStartCommand состояние isRecording:" + isRecording);
        if (isRecording) return 0;

        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        Boolean shouldRecord = prefs.getBoolean(PrefsFragment.PREF_RECORD_CALLS, false);
        if (!shouldRecord) {
            Log.i(TAG, "Запись разговоров ОТКЛЮЧЕНА в настройках");
            return 0;
        }

        int audiosource = Integer.parseInt(prefs.getString(PrefsFragment.PREF_AUDIO_SOURCE, "1"));
        Log.i(TAG, "RecordService настроен на использование MediaRecorder с каналом-источником " + audiosource);

        startRecording(prefs);

        return 0;
    }



    public void onDestroy()
    {
        super.onDestroy();
        if (isRecording) stopRecording();
    }


    // methods to handle binding the service

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public boolean onUnbind(Intent intent)
    {
        return false;
    }

    public void onRebind(Intent intent)
    {
    }


    private void updateNotification(String status, String filename)
    {
        Context c = getApplicationContext();
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notify));
        builder.setContentTitle("FLAT|EARS : запись звонков");
        //builder.setOngoing(true); // :: permanent Notification

        if (status.equals("IDLE")) {

            builder.setSmallIcon(R.drawable.icn_pref);
            builder.setContentText("Разговор будет записан");
            builder.setSubText("Нажмите для настройки");

        }

        if (status.equals("RECDONE")) {

            Intent playIntent = new Intent(c, CallPlayer.class); //Intent.ACTION_VIEW);
            Uri uri = Uri.parse(filename);
            playIntent.setData(uri);
            playIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//not recommend
            startActivity(playIntent);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, playIntent, 0);
            builder.setContentIntent(pendingIntent);
            builder.setSmallIcon(R.drawable.icn_play);
            builder.setContentText("ЗАПИСЬ ЗАВЕРШЕНА");
            builder.setSubText("Нажмите, чтобы прослушать");

            status = "IDLE";
        }

        if (status.equals("RECSTART")) {

            builder.setSmallIcon(R.drawable.icn_norm);
            builder.setContentText("ПРОИЗВОДИТСЯ ЗАПИСЬ");
            builder.setSubText("Разговор с абонентом " + incoming);

        }

        // Set the notification to auto-cancel. This means that the notification will disappear
        // after the user taps it, rather than remaining until it's explicitly dismissed.
        //builder.setAutoCancel(true);
        //builder.setContentText("Канал источник: " + prefs.getString(PrefsFragment.PREF_AUDIO_SOURCE, "1") + " :: " + when + "сек");

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra)
    {
        Log.i(TAG, "RecordService got MediaRecorder onInfo callback with what: " + what + " extra: " + extra);
        isRecording = false;
        updateNotification ("IDLE", "");
    }

    // MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra)
    {
        Log.e(TAG, "RecordService got MediaRecorder onError callback with what: " + what + " extra: " + extra);
        isRecording = false;
        mr.release();
        updateNotification ("IDLE", "");
    }


    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,FLATEARS_FOLDER_LOCATION);

        if(!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                Log.e("CallRecorder", "RecordService:: unable to create directory " + file + ": " + e);
            }
        };

        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simSerialNumber = telemamanger.getSimSerialNumber();
        return (simSerialNumber + "-" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }


    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,FLATEARS_FOLDER_LOCATION);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (AUDIO_RECORDER_TEMP_FILE);
    }


    private void startRecording(SharedPreferences prefs){

        int audiosource = Integer.parseInt(prefs.getString(PrefsFragment.PREF_AUDIO_SOURCE, "1"));
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        try {
            recorder = new AudioRecord(audiosource, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize );
        }
        catch (java.lang.IllegalArgumentException e) {
            Log.w(TAG, "Неверный аудиоисточник: "+e);
            //recreate recorder with mic audiosource
            recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize );
            downgradeSource(prefs);
        }


        // :: MAKE SOME TEST, If WE CANNOT READ THEN CHANGE SOURCE AND RECREATE RECORDER ::
        if (audiosource == MediaRecorder.AudioSource.VOICE_CALL) {
            if (recorder.read(new byte[2], 0, 2) == AudioRecord.ERROR_INVALID_OPERATION) {
                recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize );
                downgradeSource(prefs);
            }
        }

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();
        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();

        writedb(audiosource);
    }


    public void downgradeSource(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsFragment.PREF_AUDIO_SOURCE, ""+MediaRecorder.AudioSource.MIC);
        //editor.commit();
        editor.apply();
    }


    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
        String filename = DEFAULT_STORAGE_LOCATION + getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Чтение из источника: Invalid operation error");
                    break;
                } else if (read == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Чтение из источника: Bad value error");
                    break;
                } else if (read == AudioRecord.ERROR) {
                    Log.e(TAG, "Чтение из источника: Unknown error");
                    break;
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void writedb(int audiosource) {

        TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String getSimSerialNumber = telemamanger.getSimSerialNumber();
        String getDeviceID = telemamanger.getDeviceId();
        String getAndroidID =  android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        cv.put(RecordingProvider.KEY_LINTIME, new Date().getTime());
        cv.put(RecordingProvider.KEY_ANUM, getSimSerialNumber);
        cv.put(RecordingProvider.KEY_DEVID, getDeviceID);
        cv.put(RecordingProvider.KEY_ANDROIDID, getAndroidID);
        cv.put(RecordingProvider.KEY_FORMAT, "WAV");

        switch (audiosource) {
            case 1:
                cv.put(RecordingProvider.KEY_SOURCE, "MIC");
                break;
            case 4:
                cv.put(RecordingProvider.KEY_SOURCE, "LINE");
                break;
            default: {
                cv.put(RecordingProvider.KEY_SOURCE, ""+audiosource);
            }
        }

        cv.put(RecordingProvider.KEY_LINK, getFilename());

        Uri uri = getContentResolver().insert(RecordingProvider.CONTENT_URI, cv);
        rowID = uri.getLastPathSegment();
        Log.i("FLATEARS", "Добавлена запись с индексом " + rowID);

    }


    private void stopRecording(){
        if(null != recorder){
            isRecording = false;

            int i = recorder.getState();
            if(i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        String outputFile = cv.get(RecordingProvider.KEY_LINK).toString();
        copyWaveFile(getTempFilename(),outputFile);
        updateNotification ("RECDONE", outputFile);
        deleteTempFile();

    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE/2;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(DEFAULT_STORAGE_LOCATION + inFilename);
            out = new FileOutputStream(DEFAULT_STORAGE_LOCATION + outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            //AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
