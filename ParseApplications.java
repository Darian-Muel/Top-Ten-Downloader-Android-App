package com.example.top10downloader;

import android.nfc.Tag;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

public class ParseApplications {
    private static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> applications;

    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String xmlData) {
        //bool to return weather data could be parsed or not
        boolean status = true;
        //stores details of xml
        FeedEntry currentRecord = null;
        //tells us if were at the stage where we are processing a entry
        boolean inEntry = false;
        //stores the value of the current tag
        String textValue = "";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();
            //int variable to save the rank of the song or app
            int rank = 0;
            //check to make sure we are not at the end of the xml
            while(eventType != XmlPullParser.END_DOCUMENT) {
                //stores name of current tag
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
//                        Log.d(TAG, "parse: Starting tag for " + tagName);
                        if("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry();

                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
//                        Log.d(TAG, "parse: Ending tag for " + tagName);
                        if(inEntry == true) {
                            //write all if statements with a string instead of tagname because the xmlpullparser.getname can return null making a null expection
                            //ending tag of entry, add feed info to the listarray
                            if("entry".equalsIgnoreCase(tagName)) {
                                rank += 1;
                                currentRecord.setRank("#" + Integer.toString(rank));
                                applications.add(currentRecord);
                                inEntry = false;
                            }
                            else if("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            }
                            else if("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            }
                            else if("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            }
                            else if("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            }
                            else if("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;

                    default:
                        //nothing else to do
                }
                //go to next event
                //tells parser to continue working through the xml
                eventType = xpp.next();

            }
            //code to test to make sure all the info in FeedEntry is correct
            // only used for testing
//            for (FeedEntry app: applications) {
//                Log.d(TAG, "*****************");
//                Log.d(TAG, app.toString());
//            }

        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }
        return status;
    }
}
