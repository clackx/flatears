package flynn.pro.flatears;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends Fragment {

    private ViewGroup mContainerView;
    String[] dlist;


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
        ((TextView) newView.findViewById(android.R.id.text1)).setText(name);

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

        // Because mContainerView has android:animateLayoutChanges set to true,
        // adding this view is automatically animated.
        mContainerView.addView(newView, 0);
    }




}
