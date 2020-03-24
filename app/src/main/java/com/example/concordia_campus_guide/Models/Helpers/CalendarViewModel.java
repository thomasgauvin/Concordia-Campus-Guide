package com.example.concordia_campus_guide.Models.Helpers;

import android.Manifest;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;

import com.example.concordia_campus_guide.Activities.SearchActivity;
import com.example.concordia_campus_guide.Models.CalendarEvent;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CalendarViewModel extends AndroidViewModel {
    Context context;

    public CalendarViewModel(@NonNull Application application){
        super(application);
        context = application.getApplicationContext();
    }

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX =0;
    private static final int PROJECTION_TITLE_INDEX = 1;
    private static final int PROJECTION_LOCATION_INDEX = 2;
    private static final int PROJECTION_START_INDEX = 3;

    public static final String[] INSTANCE_PROJECTION = new String[] {
            Instances._ID,
            Instances.TITLE,
            Instances.EVENT_LOCATION,
            Instances.DTSTART
    };

    public CalendarEvent getEvent(SearchActivity searchActivity) {
        if (!hasReadPermission()) {
            ActivityCompat.requestPermissions(searchActivity,
                    new String[]{Manifest.permission.READ_CALENDAR}, 101);
        }
        if(!hasWritePermission()) {
            ActivityCompat.requestPermissions(searchActivity,
                    new String[]{Manifest.permission.WRITE_CALENDAR}, 123);
        }
        else {
            Cursor cursor = getCalendarCursor();
            insertAppUrlInCalendarEvent(cursor);
            return getCalendarEvent(cursor);
        }
        return null;
    }

    public void insertAppUrlInCalendarEvent(Cursor cursor){
        ContentResolver cr = context.getContentResolver();
        while (cursor.moveToNext()) {
            Uri updateUri = null;
            String eventTitle = cursor.getString(PROJECTION_TITLE_INDEX);
            if(eventTitle.contains("Lecture: ")||eventTitle.contains("Tutorial: ") || eventTitle.contains("Lab: ")){
                Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                        .buildUpon();
                long eventID = cursor.getLong(PROJECTION_ID_INDEX);
                Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);

                ContentValues event = new ContentValues();

                event.put(CalendarContract.Events.DESCRIPTION, "Hampic is cute :3");
                event.put(CalendarContract.Events.TITLE, "WHY?!");

                //eventUri = eventsUriBuilder.build();
                int rows = cr.update(eventUri,event,null,null);
                Log.i("HELLO: ", "rows: " + rows);
            }
        }
    }

    public String getNextClassString(CalendarEvent event){
        String nextClassString = "";
            if(event != null){
                Date eventDate = new Date((Long.parseLong(event.getStartTime())));
                String timeUntil = getTimeUntilString(eventDate.getTime(), System.currentTimeMillis());
                nextClassString = event.getTitle() +  " in " + timeUntil;
            }else{
                nextClassString = "No classes today";
            }
        return  nextClassString;
    }


    public Cursor getCalendarCursor() {
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        long startTime = new Date().getTime();
        long endTime = startTime + 28800000;
        ContentUris.appendId(eventsUriBuilder, startTime);
        ContentUris.appendId(eventsUriBuilder, endTime);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = context.getContentResolver().query(
                eventsUri,
                INSTANCE_PROJECTION,
                null,
                null,
                Instances.BEGIN + " ASC"
        );
        return cursor;
    }

    public CalendarEvent getCalendarEvent(Cursor cursor) {
        while (cursor.moveToNext()) {
            int eventID = cursor.getInt(PROJECTION_ID_INDEX);
           String eventTitle = cursor.getString(PROJECTION_TITLE_INDEX);
           String eventLocation = cursor.getString(PROJECTION_LOCATION_INDEX);
           String eventStart = cursor.getString(PROJECTION_START_INDEX);
            Log.i("FOCUS: ", Integer.toString(eventID));

           if(eventTitle.contains("Lecture: ")||eventTitle.contains("Tutorial: ") || eventTitle.contains("Lab: ")){
               return new CalendarEvent(eventTitle, eventLocation, eventStart);
           }
        }
        return null;
    }

    private boolean hasReadPermission(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }
    private boolean hasWritePermission(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    private String getTimeUntilString(long eventTime, long currentTime){
        long differenceInMillis = eventTime - currentTime;

        String timeUntil = String.format("%02d hours and %02d minutes",
                TimeUnit.MILLISECONDS.toHours(differenceInMillis),
                TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(differenceInMillis)));
        return timeUntil;
    }
}
