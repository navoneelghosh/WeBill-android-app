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

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.teamblue.WeBillv2.BuildConfig;
import com.teamblue.WeBillv2.R;
import com.teamblue.WeBillv2.model.api.BillMethods;
import com.teamblue.WeBillv2.model.api.FriendRequest;
import com.teamblue.WeBillv2.model.api.VeryfiMethods;
import com.teamblue.WeBillv2.model.pojo.Constants;

import com.teamblue.WeBillv2.model.pojo.LoginModel;
import com.teamblue.WeBillv2.model.pojo.OCRBill;
import com.teamblue.WeBillv2.model.pojo.VeryfiOcrResponse;
import com.teamblue.WeBillv2.service.LoginRetrofitClient;
import com.teamblue.WeBillv2.service.VeryfiRetrofitClient;
import com.teamblue.WeBillv2.view.SplitBillActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    private static final String TAG2 = "veryfi";
    private Uri uri;

    private String Base64String, currentPhotoPath;

    ViewGroup container;


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
            //imageBitmap = (Bitmap) extras.get("data");

//            try{
//                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), uri);
//            }catch (Exception e){
//                imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
//            }
            imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);

            // initialize byte stream
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            // compress Bitmap
            imageBitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
            // Initialize byte array
            byte[] bytes=stream.toByteArray();
            // get base64 encoded string
            Base64String= Base64.encodeToString(bytes,Base64.DEFAULT); // send the string to backend

            /********* TODO: Hard Coded scanned result now. Replace once finished real API ******/
            try {
                scanBillFromApi(getLayoutInflater(),container,Base64String);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanBillFromApi(LayoutInflater layoutInflater,ViewGroup container,String filedata){
        Log.d(TAG2,"inside scan bill");
        VeryfiMethods veryfiMethods = VeryfiRetrofitClient.getRetrofitInstance().create(VeryfiMethods.class);
        OCRBill ocrBill = new OCRBill();
        ocrBill.setBillName("example.jpg");
        ocrBill.setBase64encodedString(filedata);
        Call<VeryfiOcrResponse> call = veryfiMethods.processBill(ocrBill);

        call.enqueue(new Callback<VeryfiOcrResponse>() {
            @Override
            public void onResponse(Call<VeryfiOcrResponse> call, Response<VeryfiOcrResponse> response) {
                Log.d(TAG2,String.valueOf(response.code()));
                VeryfiOcrResponse veryfiOcrResponse = new VeryfiOcrResponse();
                if(response.code()==Constants.RESPONSE_OK){
                    Log.d(TAG2,"success getting bill");
                    veryfiOcrResponse = (VeryfiOcrResponse) response.body();
                    Log.d(TAG2,veryfiOcrResponse.toString());
                    View view = layoutInflater.inflate(R.layout.fragment_add_bill, container, false);
                    edtActivityNameAddBill = (EditText) view.findViewById(R.id.edtActivityNameAddBill);
                    edtTotalAmountAddBill = (EditText) view.findViewById(R.id.edtTotalAmountAddBill);
                    edtDateAddBill = (EditText) view.findViewById(R.id.edtDateAddBill);
                    edtAddressAddBill = (EditText) view.findViewById(R.id.edtAddressAddBill);
                    btnEnterAddBill = (Button) view.findViewById(R.id.btnEnterAddBill);
                    btnScanBill = (Button) view.findViewById(R.id.btnScanBill);

                    edtActivityNameAddBill.setText(veryfiOcrResponse.getVendor().getName());
                    edtTotalAmountAddBill.setText(String.valueOf(veryfiOcrResponse.getTotal()));
                    edtDateAddBill.setText(veryfiOcrResponse.getDate());
                    edtAddressAddBill.setText(veryfiOcrResponse.getVendor().getAddress());

                    container.removeAllViews();
                    container.addView(view);

                }
            }

            @Override
            public void onFailure(Call<VeryfiOcrResponse> call, Throwable t) {
                Log.d(TAG2,"on failure");
                Toast.makeText(getContext(),t.getMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//prevent keyboard pushing my view
        View view = inflater.inflate(R.layout.fragment_add_bill, container, false);
        this.container = container;
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
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(takePictureIntent.resolveActivity(getActivity().getPackageManager())!=null){
                    File photoFile = null;
                    try{
                        photoFile = createPhotoFile();
                    }catch (IOException ioException){
                        Log.d(TAG2,ioException.getMessage());
                    }

                    if(photoFile!=null){
                        Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.android.fileprovider", photoFile);
                        //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        uri = photoURI;
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
                        //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }

//                    startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);

            }
        });

        return view;
    }

    private File createPhotoFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
