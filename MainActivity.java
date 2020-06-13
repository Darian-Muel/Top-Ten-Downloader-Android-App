package com.example.top10downloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private String feedCachedUrl = "INVALIDATED";

    private static final String STATE_FEEDURL = "feedUrl";
    private static final String STATE_FEEDLIMIT = "feedLimit";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);
        // if savedInstanceState bundle is null it means we are running the app for the first time
        // so if the phone is rotated restore the values needed when downloadUrl is called
        if (savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(STATE_FEEDURL);
            feedLimit = savedInstanceState.getInt(STATE_FEEDLIMIT);
        }
        downloadUrl(String.format(feedUrl, feedLimit));

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_FEEDURL, feedUrl);
        outState.putInt(STATE_FEEDLIMIT, feedLimit);
        super.onSaveInstanceState(outState);

    }


    @Override
    // is called when its time to inflate the the actvities menu, creates the menu objects from the xml file
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the feeds_menu.xml
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if (feedLimit == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        //tell android that we inflated a menu
        return true;

    }

    @Override
    //when this method is called android passes in the menuitem that is selcted from the menu xml
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //return the identifer for this menu item
        int id = item.getItemId();


        switch (id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnuRefresh:
                //by changing feedCachedUrl we are able to bypass the first if statement in downloadUrl and redownload the url
                feedCachedUrl = "INVALIDATED";
                break;
            case R.id.mnu10:
                if (item.isChecked() != true) {
                    item.setChecked(true);
                    feedLimit = 10;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedLimit unchanged");
                }
                break;
            case R.id.mnu25:
                if (item.isChecked() != true) {
                    item.setChecked(true);
                    feedLimit = 25;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedLimit unchanged");
                }
                break;
            default:
                //important, should always be included in code that reacts to menu choices
                // it should never excute since we have covered all possible menu choices above
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(String.format(feedUrl, feedLimit));
        return true;

    }

    private void downloadUrl(String feedUrl) {
        // only download the url if it has not already been downloaded
        if (feedUrl.equalsIgnoreCase(feedCachedUrl) != true) {
            Log.d(TAG, "downloadUrl: starting Asynctask");
            DownloadData downloadData = new DownloadData();
            //calls doInBackground method
            downloadData.execute(feedUrl);
            feedCachedUrl = feedUrl;
            Log.d(TAG, "downloadUrl: done");
        } else {
            Log.d(TAG, "downloadUrl: URL not changed");
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        //calls after doInBackground method is finished
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            //display FeedEntrys in the listView
//            listApps.setAdapter(arrayAdapter);

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);

        }

        @Override
        protected String doInBackground(String... strings) {
//            Log.d(TAG, "doInBackGround: Starts with " + strings[0]);
            // return of doInBackground gets passed to the onPostExecute
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // will store the status code from the HTTP, ex 200(ok) or 401(Unauthorized)
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was " + response);
//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);
                // bufferedReader reads chars not strings
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                //char array to store 500 characters
                char[] inputBuffer = new char[500];
                charsRead = reader.read(inputBuffer);
                // if charsread is -1 no more data is left to append
                while (charsRead > 0) {
                    xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    charsRead = reader.read(inputBuffer);
                }
                reader.close();

                return xmlResult.toString();

                // always put the MalformedURLException above the IOException because malformed is a subclass of IOException
            } catch (MalformedURLException e) {
                // e.getmessage gets more info about the error that happened
                Log.e(TAG, "downloadXML: Invalid URL " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception. Needs permission? " + e.getMessage());
//                e.printStackTrace();
            }
            return null;
        }
    }
}