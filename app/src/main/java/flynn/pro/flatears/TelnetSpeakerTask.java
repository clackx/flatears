package flynn.pro.flatears;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by claqx on 08.08.16.
 */

public class TelnetSpeakerTask extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String response = "";
    ContentValues cv = new ContentValues();

    String TAG = "TLNTSPKR";

    TelnetSpeakerTask(String addr, int port, ContentValues values){
        dstAddress = addr;
        dstPort = port;
        cv = values;
    }

    //declare a delegate with type of protocol declared in this task
    private TaskDelegate delegate;

    //here is the task protocol to can delegate on other object
    public interface TaskDelegate {
        //define you method headers to override
        void onTaskEndWithResult(int success);
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        Socket socket = null;
        PrintWriter out = null;
        boolean isanswer = false, isauthen = false, isauthor = false;

        try {
            socket = new Socket(dstAddress, dstPort);
            out = new PrintWriter(socket.getOutputStream(), true);

            InputStream in = socket.getInputStream();
            StringBuilder sb=new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String read;

            read=br.readLine();
            System.out.println(read);
            sb.append(read);
            response = sb.toString();
            sb.setLength(0);

            if (response.contains("Welcome")) {
                isanswer = true;
            }
            else Log.d(TAG, "NO ANSWER");

            if (isanswer) {
                out.println("tokenauth: 34346HnKms*9);L\r\n");
                Log.d(TAG, "AUTH SEND");

                read=br.readLine();
                sb.append(read);
                response = sb.toString();
                Log.d(TAG,"AUTH: "+response);
                sb.setLength(0);

                if (response.contains("200: OK")) {
                    isauthen = true;
                }
                else Log.d(TAG,"AUTH FAIL");

                if (isauthen) {
                    out.println("simid:"+cv.get(RecordingProvider.KEY_ANUM)+"\r\n");
                    Log.d(TAG,"ID SEND: "+cv.get(RecordingProvider.KEY_ANUM));

                    read=br.readLine();
                    sb.append(read);
                    response = sb.toString();
                    Log.d(TAG,"ID: "+response);
                    sb.setLength(0);

                    if (response.contains("200: OK")) {
                        isauthor = true;
                    }

                    if (isauthor) {

                        String time = (""+cv.get(RecordingProvider.KEY_TIME)).replace(":", ".");
                        SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss");
                        SimpleDateFormat sdf_norm = new SimpleDateFormat("dd.MM.yyyy");
                        SimpleDateFormat sdf_flat = new SimpleDateFormat("yyyy-MM-dd");
                        String durationInSec = ""+cv.get(RecordingProvider.KEY_DURATION);
                        if (durationInSec.equals("null")) durationInSec="0";
                        long millis = Integer.parseInt(durationInSec)*1000;
                        final long hr = TimeUnit.MILLISECONDS.toHours(millis);
                        final long min = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hr));
                        final long sec = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
                        String timeFormated = String.format("%02d.%02d.%02d", hr, min, sec);
                        String dateFormated = sdf_flat.format(sdf_norm.parse(""+cv.get(RecordingProvider.KEY_DATE)));

                        String insertRecord = "insertrecord:"+
                                cv.get(RecordingProvider.KEY_CALLTYPE)+":"+
                                dateFormated+":"+ time+":"+
                                timeFormated+":"+
                                cv.get(RecordingProvider.KEY_LINK)+":"+
                                cv.get(RecordingProvider.KEY_BNUM)+"\r\n";
                        out.println(insertRecord);
                        Log.d(TAG, "INSERT: "+insertRecord);
                        //out.println("insertrecord:outgoing:21.05.16:13.14:0:filename.example:89123456789\r\n");

                        read=br.readLine();
                        System.out.println(read);
                        sb.append(read);
                        response = sb.toString();
                    }
                    else Log.i(TAG,"AUTHORIZATION FAIL. НЕОБХОДИМО ДОБАВИТЬ ПОЛЬЗОВАТЕЛЯ");
                }

            }
            System.out.println("received: "+response);

        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            response = "ConnectException: " + e;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "IOEXception " + e);
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //@Override
    protected void onPostExecute(Integer result) {
        //textResponse.setText(response);
        if (delegate != null) {
            //return success or fail to activity
            delegate.onTaskEndWithResult(result);
            //super.onPostExecute(result);
        }

    }

}
