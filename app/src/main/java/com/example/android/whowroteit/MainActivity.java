/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.whowroteit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * The WhoWroteIt app queries the Book Search API for books based
 * on a user's search.  It uses an AsyncTask to run the search task in
 * the background.
 */
public class MainActivity extends AppCompatActivity {

    // Variables for the search input field and results TextViews.
    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;
    private String mQueryString;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput = findViewById(R.id.bookInput);
        mTitleText = findViewById(R.id.titleText);
        mAuthorText = findViewById(R.id.authorText);
        mImageView = findViewById(R.id.imageView);

    }

    /**
     * onClick handler for the "Search Books" button.
     *
     * @param view The view (Button) that was clicked.
     */
    public void searchBooks(View view) {
        // Get the search string from the input field.

        final String BOOK_BASE_URL =
                "https://www.googleapis.com/books/v1/volumes?";
        // Parameter for the search string.
        final String QUERY_PARAM = "q";
        // Parameter that limits search results.
        final String MAX_RESULTS = "maxResults";
        // Parameter to filter by print type.
        final String PRINT_TYPE = "printType";

        mQueryString = mBookInput.getText().toString();
        // Hide the keyboard when the button is pushed.
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Check the status of the network connection.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If the network is available, connected, and the search field
        // is not empty, start a FetchBook AsyncTask.
        if (networkInfo != null && networkInfo.isConnected()
                && mQueryString.length() != 0) {

            //new FetchBook(mTitleText, mAuthorText).execute(mQueryString);
            mAuthorText.setText("");
            mTitleText.setText(R.string.loading);

            Uri builtURI = Uri.parse(BOOK_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, mQueryString)
                    .appendQueryParameter(MAX_RESULTS, "10")
                    .appendQueryParameter(PRINT_TYPE, "books")
                    .build();
            String url = builtURI.toString();

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONArray itemsArray = jsonObject.getJSONArray("items");
                                    int i = 0;
                                    String title = null;
                                    String authors = null;
                                    String thumbnailImg;


                                    while (i < itemsArray.length() &&
                                            (authors == null && title == null)) {
                                        // Get the current item information.
                                        JSONObject book = itemsArray.getJSONObject(i);
                                        JSONObject volumeInfo = book.getJSONObject("volumeInfo");
                                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                                        // Try to get the author and title from the current item,
                                        // catch if either field is empty and move on.
                                        try {
                                            title = volumeInfo.getString("title");
                                            Log.d("Image",title);
                                            authors = volumeInfo.getString("authors");
                                            Log.d("Image",authors);

                                            thumbnailImg = imageLinks.getString("thumbnail");
                                            Log.d("Image",thumbnailImg);

                                            if (title != null && authors != null) {
                                                mTitleText.setText(title);
                                                mAuthorText.setText(authors);
                                                Picasso.get().setLoggingEnabled(true);
                                                Picasso.get().load(thumbnailImg).into(mImageView);

                                            } else {
                                                mTitleText.setText(R.string.no_results);
                                                mAuthorText.setText("");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        // Move to the next item.
                                        i++;
                                    }
                                } catch (Exception e) {
                                    // If onPostExecute does not receive a proper JSON string,
                                    // update the UI to show failed results.
                                    mTitleText.setText(R.string.no_results);
                                    mAuthorText.setText("");
                                }
                            }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {}
            });
            queue.add(stringRequest);


        }
        // Otherwise update the TextView to tell the user there is no
        // connection, or no search term.
        else {
            if (mQueryString.length() == 0) {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_search_term);
            } else {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_network);
            }
        }
    }

    public void searchBooksVolley(View view) {
//        RequestQueue queue = Volley.newRequestQueue(this);
//        mQueryString = mBookInput.getText().toString();
//        // Constants for the various components of the Books API request.
//        //
//        // Base endpoint URL for the Books API.
//
//
//
//        // Build the full query URI, limiting results to 10 items and
//        // printed books.
//
//
//        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, builtURI.toString(), null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            String data = response.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("smallThumbmail");
//                            Log.d("Result", data );
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        // Do something with response - can update the UI directly here
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {}
//        });
//        queue.add(jsonRequest);
//
    }
}
