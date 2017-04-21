package prashanth.wesync;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import prashanth.wesync.models.ContactList;
import prashanth.wesync.models.UserInfo;

import static prashanth.wesync.AppConstants.PERMISSION_REQUEST_LOCATION;


public class DestinationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<UserInfo> dbUsersFromPhone = new ArrayList<>();
    ArrayList<ContactList> UsersFromPhone;
    Address address;

    ArrayList<String> matchedUserNames = new ArrayList<>();

    ArrayList<UserInfo> userList = new ArrayList<>();

    ArrayList<String> phoneNo = new ArrayList<>();
    ArrayList<String> emailIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     *
     * @param view
     */
    public void onSearch(View view) {


        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot topSnapshot) {

                    for (DataSnapshot snapshot : topSnapshot.getChildren()) {
                        UserInfo user = snapshot.getValue(UserInfo.class);
                        phoneNo.add(user.getPhoneNumber());
                        emailIds.add(user.getEmail());
                        userList.add(user);
                    }
                    getMatchingContacts();

                    for(UserInfo user:dbUsersFromPhone){
                        float[] results = new float[1];
                        Location.distanceBetween(address.getLatitude(), address.getLongitude(), user.getLatitude(), user.getLongitude(), results);
                        float distanceInMeters = results[0];
                        boolean isWithin10km = distanceInMeters < 10000;
                        if(isWithin10km){
                            LatLng latLngFriends = new LatLng(user.getLatitude(), user.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(latLngFriends).title(user.getName()));
                        }
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });




        }

    }

    private void getMatchingContacts() {
        UsersFromPhone = ((GlobalClass) this.getApplication()).getContactList();
        for (ContactList contact : UsersFromPhone) {
            if (phoneNo.contains(contact.getContactNo())) {
                if (!matchedUserNames.contains(contact.getContactName())) {
                    matchedUserNames.add(contact.getContactName());
                    dbUsersFromPhone.add(userList.get(phoneNo.indexOf(contact.getContactNo())));
                }
            } else if (emailIds.contains(contact.getEmail())) {
                if (!matchedUserNames.contains(contact.getContactName())) {
                    matchedUserNames.add(contact.getContactName());
                    dbUsersFromPhone.add(userList.get(emailIds.indexOf(contact.getEmail())));

                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else{
            mMap.setMyLocationEnabled(true);
        }

    }


    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this,"Permission required for app to run", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }
}
