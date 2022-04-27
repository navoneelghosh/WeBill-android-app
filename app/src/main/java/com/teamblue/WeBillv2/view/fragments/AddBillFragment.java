package com.teamblue.WeBillv2.view.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.teamblue.WeBillv2.BuildConfig;
import com.teamblue.WeBillv2.R;
import com.teamblue.WeBillv2.view.AddBillView;
import com.teamblue.WeBillv2.view.MainActivity;
import com.teamblue.WeBillv2.view.ScanBillActivity;
import com.teamblue.WeBillv2.view.SplitBillActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 *  addbill fragment subclass.
 */
public class AddBillFragment extends Fragment {


    private static int AUTOCOMPLETE_REQUEST_CODE = 7001;
    private EditText edtActivityNameAddBill,edtTotalAmountAddBill,edtDateAddBill,edtAddressAddBill;
    private Button btnEnterAddBill,btnScanBill;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;
    private static final String TAG = "BASE64";
    private String Base64String, currentPhotoPath;

//    private ImageView testPicture;
    public AddBillFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // actually populate the address field with the user's selection
                Place place = Autocomplete.getPlaceFromIntent(data);
                edtAddressAddBill.setText(place.getAddress());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(getContext(), "Address not found", Toast.LENGTH_SHORT).show();
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // The user canceled the operation
            }
            return;
        }

        /***************Handling Receipt Scanning Camera Here ************/
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            //5.6 then we use bundle to retrieve information from Bitmap
//            Bundle extras = data.getExtras();
//            imageBitmap = (Bitmap) extras.get("data");
//            captureIV.setImageBitmap(imageBitmap);
//            captureIV.setRotation(90);
            /*******New Solution for high resolution image ****/
            imageBitmap = BitmapFactory.decodeFile(currentPhotoPath); // Technically this is the full size image
//            testPicture.setImageBitmap(imageBitmap);
//            testPicture.setRotation(90);


            // initialize byte stream
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            // compress Bitmap
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            // Initialize byte array
            byte[] bytes=stream.toByteArray();
            // get base64 encoded string
            Base64String= Base64.encodeToString(bytes,Base64.DEFAULT); // send the string to backend
            // set encoded text on textview
//            resultTV.setText(Base64String);
            Log.d(TAG, Base64String);

            /********* TODO: Hard Coded scanned result now. Replace once finished real API ******/
            edtActivityNameAddBill.setText("Sushi(HardCode message)");
            edtTotalAmountAddBill.setText("999");
            edtDateAddBill.setText("1970/01/01");
            edtAddressAddBill.setText("HardCode Address Here");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//prevent keyboard pushing my view
        View view = inflater.inflate(R.layout.fragment_add_bill, container, false);

        edtActivityNameAddBill = (EditText) view.findViewById(R.id.edtActivityNameAddBill);
        edtTotalAmountAddBill = (EditText) view.findViewById(R.id.edtTotalAmountAddBill);
        edtDateAddBill = (EditText) view.findViewById(R.id.edtDateAddBill);
        edtAddressAddBill = (EditText) view.findViewById(R.id.edtAddressAddBill);
        btnEnterAddBill = (Button) view.findViewById(R.id.btnEnterAddBill);
        btnScanBill = (Button) view.findViewById(R.id.btnScanBill);

//        testPicture = (ImageView) view.findViewById(R.id.testPicture);


        // Initialize the Google Maps Places SDK and create PlacesClient instance
        // we need this for the autocomplete feature in the address field
        if (!Places.isInitialized())
            Places.initialize(getContext(), BuildConfig.MAPS_API_KEY);
        PlacesClient placesClient = Places.createClient(getContext());

        // set to the autocomplete feature to activate after the user clicks on the address field
        edtAddressAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set the fields to specify which types of place data to return after the user
                // has made a selection
                List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG);

                // start the autocomplete intent
                Intent autocompleteIntent = new Autocomplete
                        .IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(getContext());
                startActivityForResult(autocompleteIntent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        btnEnterAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Checking for no total amount empty error
                if(TextUtils.isEmpty(edtActivityNameAddBill.getText().toString())) {
                    edtActivityNameAddBill.setError("Must Not Be Empty!");
                    return;
                }else if(TextUtils.isEmpty(edtTotalAmountAddBill.getText().toString())) {
                    edtTotalAmountAddBill.setError("Must Not Be Empty!");
                    return;
                }else if(TextUtils.isEmpty(edtDateAddBill.getText().toString())){
                    edtDateAddBill.setError("Must Not Be Empty!");
                        return;
                }else if(TextUtils.isEmpty(edtAddressAddBill.getText().toString())){
                    edtAddressAddBill.setError("Must Not Be Empty!");
                    return;
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putString("BILL_ACTIVITY_NAME",edtActivityNameAddBill.getText().toString());
                    bundle.putString("BILL_TOTAL_AMOUNT",edtTotalAmountAddBill.getText().toString());
                    bundle.putString("BILL_DATE",edtDateAddBill.getText().toString());
                    bundle.putString("BILL_ADDRESS",edtAddressAddBill.getText().toString());
                    Intent gotoSplitBillActivity = new Intent(view.getContext(), SplitBillActivity.class);
                    gotoSplitBillActivity.putExtras(bundle);
                    startActivity(gotoSplitBillActivity);
                }

            }
        });


        /***************Handling Receipt Scanning Camera Here ************/
        //Asking Permission
        if (ContextCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions((Activity) view.getContext(),
                    new String[]{Manifest.permission.CAMERA},REQUEST_IMAGE_CAPTURE);

        }

        btnScanBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent gotoScanBillActivity = new Intent(view.getContext(), ScanBillActivity.class);
//                startActivity(gotoScanBillActivity);

                /*******New Solution for high resolution image ****/
                String fileName = "receiptPhoto";
                File storageDirectory = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                try {
                    File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);

                    currentPhotoPath = imageFile.getAbsolutePath();

                    Uri imageUri = FileProvider.getUriForFile(getContext(),"com.teamblue.WeBillv2.fileprovider",imageFile);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }


}
