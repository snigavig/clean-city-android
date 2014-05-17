package ua.in.nogarbage.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks  {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private GarbageMapFragment mGarbageMapFragment;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction()
        //        .replace(R.id.container, GarbageMapFragment.newInstance(position + 1))
        //        .commit();
        Fragment fragment = null;

        switch(position) {
            case 0:
                fragment = new GarbageMapFragment();
                Bundle args = new Bundle();
                args.putInt("section_number", position + 1);
                fragment.setArguments(args);
                break;
            case 1:
                fragment = GarbageLitsFragment.newInstance(position + 1);
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        String[] mNavigationItems = getResources().getStringArray(R.array.navigation);
        mTitle = mNavigationItems[number - 1];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_create:
                Intent addIntent = new Intent(this, CreateItemActivity.class);
                startActivity(addIntent);
                return true;
            case R.id.action_settings:
                //TODO: openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        break;
                }
        }
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("_________________", "wow");

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), "_________________");
            }
            return false;
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class GarbageMapFragment extends Fragment implements
    GooglePlayServicesClient.ConnectionCallbacks,
    GooglePlayServicesClient.OnConnectionFailedListener {
        // ...
        public static final String ARG_SECTION_NUMBER = "section_number";
        static final LatLng CHASOPYS = new LatLng(50.439467, 30.514971);
        static final LatLng CIKLUM = new LatLng(50.439204, 30.523619);
        private GoogleMap map;
        private LocationClient mLocationClient;
        private Location mCurrentLocation;
        private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


        /*
     * Called when the Activity is no longer visible.
     */
        @Override
        public void onStop() {
            // Disconnecting the client invalidates it.
            mLocationClient.disconnect();
            super.onStop();
        }

        @Override
        public void onStart() {
            super.onStart();
            // Connect the client.
            mLocationClient.connect();
        }
        /*
         * Called by Location Services when the request to connect the
         * client finishes successfully. At this point, you can
         * request the current location or start periodic updates
         */
        @Override
        public void onConnected(Bundle dataBundle) {
            // Display the connection status
            Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();

            mCurrentLocation = mLocationClient.getLastLocation();


            Toast.makeText(getActivity(), mCurrentLocation.toString() , Toast.LENGTH_SHORT).show();
            // Move the camera instantly to hamburg with a zoom of 15.
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 18), 3000, null);

        }

        /*
         * Called by Location Services if the connection to the
         * location client drops because of an error.
         */
        @Override
        public void onDisconnected() {
            // Display the connection status
            Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
                    Toast.LENGTH_SHORT).show();
        }

        /*
         * Called by Location Services if the attempt to
         * Location Services fails.
         */
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
            if (connectionResult.hasResolution()) {
                try {
                    // Start an Activity that tries to resolve the error
                    connectionResult.startResolutionForResult(
                            getActivity(),
                            CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
                } catch (IntentSender.SendIntentException e) {
                    // Log the error
                    e.printStackTrace();
                }
            } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
                this.showErrorDialog(connectionResult.getErrorCode());
            }
        }

        private boolean showErrorDialog(int errorCode) {
            int resultCode =
                    GooglePlayServicesUtil.
                            isGooglePlayServicesAvailable(getActivity());
            // If Google Play services is available
            if (ConnectionResult.SUCCESS == resultCode) {
                // In debug mode, log the status

                // Continue
                return true;
                // Google Play services was not available for some reason
            } else {
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                // If Google Play services can provide an error dialog
                if (errorDialog != null) {
                    // Create a new DialogFragment for the error dialog
                    ErrorDialogFragment errorFragment =  new ErrorDialogFragment();
                    // Set the dialog in the DialogFragment
                    errorFragment.setDialog(errorDialog);
                    // Show the error dialog in the DialogFragment
                    errorFragment.show(getActivity().getSupportFragmentManager(),"Location Updates");

                } return false;
            }
        }

        @Override
        public void onDestroyView() {

            FragmentManager fm = getFragmentManager();

            Fragment xmlFragment = fm.findFragmentById(R.id.garbageMap);
            if (xmlFragment != null) {
                fm.beginTransaction().remove(xmlFragment).commit();
            }

            super.onDestroyView();
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_garbage_map, container, false);

            mLocationClient = new LocationClient(getActivity(), this, this);

            map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.garbageMap))
                    .getMap();

            Marker hamburg = map.addMarker(new MarkerOptions().position(CIKLUM)
                    .title("Hamburg"));

            // Move the camera instantly to hamburg with a zoom of 15.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(CIKLUM, 18));

            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);

            //...

            return v;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class GarbageLitsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GarbageLitsFragment newInstance(int sectionNumber) {
            GarbageLitsFragment fragment = new GarbageLitsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public GarbageLitsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

                    return inflater.inflate(R.layout.fragment_garbage_list, container, false);

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
