package flynn.pro.flatears;


import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends Fragment {

    public static final String BASE_PATH = "content://flynn.pro.flatears/records";
    private ViewGroup mContainerView;
    String[] dlist;
    Dialog dialog;
    TextView showBtn, cancelBtn;


    public static UploadFragment newInstance() {
        UploadFragment fragment = new UploadFragment();
        return fragment;
    }


    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_upload, container, false);

        mContainerView = (ViewGroup) rootView.findViewById(R.id.container);

        //Toast.makeText(getActivity(), "FirstFragment.onCreateView()", Toast.LENGTH_LONG).show();
        Log.d("Fragment 1", "onCreateView");

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Toast.makeText(getActivity(), "FirstFragment.onActivityCreated()", Toast.LENGTH_LONG).show();
        Log.d("Fragment 1", "onActivityCreated");


    }

    @Override
    public void onResume() {
        super.onResume();

        mContainerView.removeAllViews();
        loadRecordingsFromDir();
    }

    private void loadRecordingsFromDir() {

        File dir = new File(RecordService.DEFAULT_STORAGE_LOCATION);
        String[] dlist = dir.list();

        if (dlist != null) {
            for (int i = 0; i < dlist.length; i++) {
                addItem(dlist[dlist.length - i - 1]);
            }
        }

    }


    private void addItem(String name) {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this.getContext()).inflate(
                R.layout.list_item_example, mContainerView, false);

        // :: SET THE FILENAME IN THE ROW
        final TextView tvName = ((TextView) newView.findViewById(R.id.callname));
        tvName.setText(name);

        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        */

        String where = RecordingProvider.KEY_LINK + " = '" + name + "'";
        Cursor mCursor = getActivity().getApplicationContext().getContentResolver().query(Uri.parse(BASE_PATH), null, where, null, null);

        String ctype = "";
        String cDat="", cNum="", cDur = "", cLT = "", cTyp="";

        // Some providers return null if an error occurs, others throw an exception
        if (null == mCursor) {

        // If the Cursor is empty, the provider found no matches
        } else if (mCursor.getCount() < 1) {


        } else {
            mCursor.moveToLast();
            //ctype = +mCursor.getString(5)+"!5!"+mCursor.getString(6)+"!6!"+mCursor.getString(7);
             cDat = mCursor.getString(2)+"  "+mCursor.getString(3);
             cNum = mCursor.getString(6);
             cDur = mCursor.getString(7);
            cLT = mCursor.getString(1);
            cTyp = mCursor.getString(4);

        }

        if (cLT != "") {
            SimpleDateFormat sdf_date = new SimpleDateFormat("dd.MM HH:mm");
            //cDat = sdf_date.format(DateFormat())
        }

        if (cNum == "") {cNum = "????????????";}
        String s= cNum+" "+cTyp+" \n"+cDat+"   ( "+cDur+" сек. )";
        SpannableString ss1=  new SpannableString(s);
        ss1.setSpan(new RelativeSizeSpan(1.2f), 0, 12, 0); // set size
        // set color
        if (cTyp == null) {cTyp ="";}
        switch (cTyp) {
            case "OUTGOING":
                ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Forest_Green)), 0, 12, 0);
                break;
            case "INCOMING":
                ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Blue_Ribbon)), 0, 12, 0);
                break;
            case "MISSED":
                ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Love_Red)), 0, 12, 0);
                break;
            default:
                ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Steel_Blue)), 0, 12, 0);
                break;
        }

        //TextView tv= (TextView) findViewById(R.id.textview);
        tvName.setText(ss1);

        //tvName.setText(cDat);

        // Set a click listener for the "X" button in the row that will remove the row.
        newView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the row from its parent (the container view).
                // Because mContainerView has android:animateLayoutChanges set to true,
                // this removal is automatically animated.
                mContainerView.removeView(newView);

                // If there are no rows remaining, show the empty view.
                if (mContainerView.getChildCount() == 0) {
                    // TODO :: FIX THIS CAUSE DO NOTHING
                    //newView.getRootView().findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }
                else {
                    //newView.getRootView().findViewById(android.R.id.empty).setVisibility(View.INVISIBLE);
                    //((TextView)view.findViewById(android.R.id.empty)).setText("XXX");
                }
            }
        });

        createDialog();

        final String finalCtype = ctype;
        final String finalCDat = cDat;
        final String finalCNum = cNum;
        final String finalCDur = cDur;
        newView.findViewById(R.id.callname).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   showDialog(finalCDat, finalCNum, finalCDur);
               }
           });


        // Because mContainerView has android:animateLayoutChanges set to true,
        // adding this view is automatically animated.
        mContainerView.addView(newView, 0);


        //SHOW BTN CLIKCED
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"CLICKED",Toast.LENGTH_LONG).show();
            }
        });

        //CANCEL
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }

    private void createDialog()
    {
        dialog=new Dialog(getContext());

        //SET TITLE
        //dialog.setTitle("Player");
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //set content
        dialog.setContentView(R.layout.customdialog_layout);

        showBtn= (TextView) dialog.findViewById(R.id.showTxt);
        cancelBtn= (TextView) dialog.findViewById(R.id.cancelTxt);
    }

    private void showDialog(String dDate, String dNum, String dDur)
    {
        TextView tvNum = (TextView)dialog.findViewById(R.id.txtNum);
        TextView tvDat = (TextView)dialog.findViewById(R.id.txtDate);
        TextView tvDet = (TextView)dialog.findViewById(R.id.txtDetails);
        tvNum.setText(dNum);
        tvDat.setText(dDate);
        tvDet.setText("Продолжительность: "+dDur+"c");

        dialog.show();
    }









}
