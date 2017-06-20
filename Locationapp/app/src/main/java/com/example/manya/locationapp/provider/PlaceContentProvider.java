package com.example.manya.locationapp.provider;

/**
 * Created by Manya on 20/06/2017.
 */
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.example.manya.locationapp.provider.PlaceContract.PlaceEntry;
public class PlaceContentProvider extends ContentProvider {
    public static final int PLACES = 100;
    public static final int PLACE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = PlaceContentProvider.class.getName();

    // Define a static buildUriMatcher method that associates URI's with their int match
    public static UriMatcher buildUriMatcher() {
        // Initialize a UriMatcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Add URI matches
        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES, PLACES);//get the places
        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES + "/#", PLACE_WITH_ID);//get the places' id
        return uriMatcher;
    }
    // Member variable for a PlaceDbHelper that's initialized in the onCreate() method
    private PlaceDbHelper mPlaceDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mPlaceDbHelper = new PlaceDbHelper(context);
        return true;
    }
    //to insert new locations and their id into the database
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the places directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned
        switch (match) {
            case PLACES:
                // Insert new values into the database
                long id = db.insert(PlaceEntry.TABLE_NAME, null, values);//return number of rows inserted into database
                if (id > 0) {//if inserted into the database
                    returnUri = ContentUris.withAppendedId(PlaceContract.PlaceEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }
    //to query for the locations in our database
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mPlaceDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            // Query for the places directory
            case PLACES:
                retCursor = db.query(PlaceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }
    //deletes a single row of location(data)
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted places
        int placesDeleted; // starts as 0
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case PLACE_WITH_ID:
                // Get the place ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                placesDeleted = db.delete(PlaceEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Notify the resolver of a change and return the number of items deleted
        if (placesDeleted != 0) {
            // A place (or more) was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of places deleted
        return placesDeleted;
    }
    //updates a single row of location(data)
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Get access to underlying database
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        // Keep track of the number of updated places
        int placesUpdated;

        switch (match) {
            case PLACE_WITH_ID:
                // Get the place ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                placesUpdated = db.update(PlaceEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items updated
        if (placesUpdated != 0) {
            // A place (or more) was updated, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of places deleted
        return placesUpdated;
    }
    //get type of data
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
