package com.example.googlelensclonedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // variables for our image view, image bitmap,
    // buttons, recycler view, adapter and array list.
    private ImageView captureIV;
    private Button snapBtn, getSearchResultsBtn;
    private RecyclerView resultsRv;
    private SearchRVAdapter searchRVAdapter;
    private ArrayList<SearchRVModel> searchRVModelArrayList;
    int REQUEST_CODE = 1;
    private ProgressBar loadingPB;
    private Bitmap imageBitmap;
    String title,link,displayed_link,snippet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureIV = findViewById(R.id.image);
        snapBtn = findViewById(R.id.idBtnSnap);
        getSearchResultsBtn = findViewById(R.id.idBtnResults);
        resultsRv = findViewById(R.id.idRVSearchResults);
        loadingPB = findViewById(R.id.idPBLoading);
        searchRVModelArrayList = new ArrayList<>();
        searchRVAdapter = new SearchRVAdapter(this, searchRVModelArrayList);
        resultsRv.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false));
        resultsRv.setAdapter(searchRVAdapter);


        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRVModelArrayList.clear();
                searchRVAdapter.notifyDataSetChanged();
                takePictureIntent();
            }
        });

        // adding on click listener for our button to search data.
        getSearchResultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRVModelArrayList.clear();
                searchRVAdapter.notifyDataSetChanged();
                loadingPB.setVisibility(View.VISIBLE);
                getResults();
            }
        });
    }

    private void getResults() {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                String searchQuery = firebaseVisionImageLabels.get(0).getText();
                getSearchResults(searchQuery);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // displaying error message.
                Toast.makeText(MainActivity.this, "Fail to detect image..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSearchResults(String searchQuery) {
        String apiKey = "f0ac1a677ee5a79a8b11dfe5037cf18f2ece544209605d36797b79b3952c09c1";
        String url = "https://serpapi.com/search.json?q=" + searchQuery+ "&location=Mumbai,India&hl=en&gl=us&google_domain=google.com&api_key=" + apiKey;
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                try {

                    JSONArray organicArray = response.getJSONArray("organic_results");
                    for (int i = 0; i < organicArray.length(); i++) {
                        JSONObject organicObj = organicArray.getJSONObject(i);
                        if (organicObj.has("title")) {
                            title = organicObj.getString("title");
                        }
                        if (organicObj.has("link")) {
                            link = organicObj.getString("link");
                        }
                        if (organicObj.has("displayed_link")) {
                            displayed_link = organicObj.getString("displayed_link");
                        }
                        if (organicObj.has("snippet")) {
                            snippet = organicObj.getString("snippet");
                        }

                        searchRVModelArrayList.add(new SearchRVModel(title, link, displayed_link, snippet));
                    }
                    // notifying our adapter class
                    // on data change in array list.
                    searchRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // displaying error message.
                Toast.makeText(MainActivity.this, "No Result found for the search query..", Toast.LENGTH_SHORT).show();
            }
        });
        // adding json object request to our queue.
        queue.add(jsonObjectRequest);
    }

    private void takePictureIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // inside on activity result method we are
        // setting our image to our image view from bitmap.
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIV.setImageBitmap(imageBitmap);
        }
    }
}
