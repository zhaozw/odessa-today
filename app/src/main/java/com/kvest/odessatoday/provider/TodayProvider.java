package com.kvest.odessatoday.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

import static com.kvest.odessatoday.provider.TodayProviderContract.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 30.05.14
 * Time: 22:43
 * To change this template use File | Settings | File Templates.
 */
public class TodayProvider extends ContentProvider {
    private TodaySQLStorage sqlStorage;

    private static final int FILM_LIST_URI_INDICATOR = 1;
    private static final int FILM_ITEM_URI_INDICATOR = 2;
    private static final int FILM_TIMETABLE_LIST_URI_INDICATOR = 3;
    private static final int FILM_TIMETABLE_ITEM_URI_INDICATOR = 4;
    private static final int CINEMA_LIST_URI_INDICATOR = 5;
    private static final int CINEMA_ITEM_URI_INDICATOR = 6;
    private static final int FULL_TIMETABLE_LIST_URI_INDICATOR = 7;
    private static final int COMMENT_LIST_URI_INDICATOR = 8;
    private static final int COMMENT_ITEM_URI_INDICATOR = 9;
    private static final int CINEMA_TIMETABLE_LIST_URI_INDICATOR = 10;
    private static final int ANNOUNCEMENTS_LIST_URI_INDICATOR = 11;
    private static final int ANNOUNCEMENTS_ITEM_URI_INDICATOR = 12;
    private static final int ANNOUNCEMENT_FILMS_LIST_VIEW_URI_INDICATOR = 13;
    private static final int PLACE_LIST_URI_INDICATOR = 14;
    private static final int PLACE_ITEM_URI_INDICATOR = 15;
    private static final int EVENT_LIST_URI_INDICATOR = 16;
    private static final int EVENT_ITEM_URI_INDICATOR = 17;
    private static final int EVENT_TIMETABLE_LIST_URI_INDICATOR = 18;
    private static final int EVENT_TIMETABLE_ITEM_URI_INDICATOR = 19;
    private static final int EVENT_TIMETABLE_VIEW_LIST_URI_INDICATOR = 20;
    private static final int COMMENTS_RATING_LIST_URI_INDICATOR = 21;
    private static final int COMMENTS_RATING_ITEM_URI_INDICATOR = 22;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH, FILM_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/#", FILM_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/" + TIMETABLE_PATH, FILM_TIMETABLE_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/" + TIMETABLE_PATH + "/#", FILM_TIMETABLE_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, CINEMAS_PATH, CINEMA_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, CINEMAS_PATH + "/#", CINEMA_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/" + TIMETABLE_PATH + "/" + FULL_PATH, FULL_TIMETABLE_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, COMMENTS_PATH, COMMENT_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, COMMENTS_PATH + "/#", COMMENT_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/" + TIMETABLE_PATH + "/" + CINEMA_VIEW_PATH, CINEMA_TIMETABLE_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/" + ANNOUNCEMENTS_PATH, ANNOUNCEMENTS_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/" + ANNOUNCEMENTS_PATH + "/#", ANNOUNCEMENTS_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, FILMS_PATH + "/" + ANNOUNCEMENTS_PATH + "/" + ANNOUNCEMENT_FILMS_VIEW_PATH, ANNOUNCEMENT_FILMS_LIST_VIEW_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, PLACES_PATH + "/#", PLACE_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, PLACES_PATH + "/#/#", PLACE_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, EVENTS_PATH, EVENT_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, EVENTS_PATH + "/#", EVENT_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, EVENTS_PATH + "/" + TIMETABLE_PATH, EVENT_TIMETABLE_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, EVENTS_PATH + "/" + TIMETABLE_PATH + "/#", EVENT_TIMETABLE_ITEM_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, EVENTS_PATH + "/" + TIMETABLE_PATH + "/" + EVENTS_VIEW_PATH, EVENT_TIMETABLE_VIEW_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, COMMENTS_RATING_PATH, COMMENTS_RATING_LIST_URI_INDICATOR);
        uriMatcher.addURI(CONTENT_AUTHORITY, COMMENTS_RATING_PATH + "/#", COMMENTS_RATING_ITEM_URI_INDICATOR);
    }

    @Override
    public boolean onCreate() {
        //create sql storage
        sqlStorage = new TodaySQLStorage(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        boolean hasSelection = !TextUtils.isEmpty(selection);

        switch (uriMatcher.match(uri)) {
            case FILM_LIST_URI_INDICATOR :
                return simpleQuery(Tables.Films.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case FILM_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.Films.TABLE_NAME, uri, projection,
                        Tables.Films.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case FILM_TIMETABLE_LIST_URI_INDICATOR:
                return simpleQuery(Tables.FilmsTimetable.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case FILM_TIMETABLE_ITEM_URI_INDICATOR:
                return simpleQuery(Tables.FilmsTimetable.TABLE_NAME, uri, projection,
                        Tables.FilmsTimetable.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case CINEMA_LIST_URI_INDICATOR :
                return simpleQuery(Tables.Cinemas.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case CINEMA_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.Cinemas.TABLE_NAME, uri, projection,
                        Tables.Cinemas.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case FULL_TIMETABLE_LIST_URI_INDICATOR :
                return simpleQuery(Tables.FilmsFullTimetableView.VIEW_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case COMMENT_LIST_URI_INDICATOR :
                return simpleQuery(Tables.Comments.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case COMMENT_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.Comments.TABLE_NAME, uri, projection,
                        Tables.Comments.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case CINEMA_TIMETABLE_LIST_URI_INDICATOR :
                return simpleQuery(Tables.CinemaTimetableView.VIEW_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case ANNOUNCEMENTS_LIST_URI_INDICATOR :
                return simpleQuery(Tables.AnnouncementsMetadata.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case ANNOUNCEMENTS_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.AnnouncementsMetadata.TABLE_NAME, uri, projection,
                        Tables.AnnouncementsMetadata.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case ANNOUNCEMENT_FILMS_LIST_VIEW_URI_INDICATOR :
                return simpleQuery(Tables.AnnouncementFilmsView.VIEW_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case PLACE_LIST_URI_INDICATOR :
                return simpleQuery(Tables.Places.TABLE_NAME, uri, projection,
                        Tables.Places.Columns.PLACE_TYPE + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case PLACE_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.Places.TABLE_NAME, uri, projection,
                        Tables.Places.Columns.PLACE_TYPE + "=" + uri.getPathSegments().get(1) +
                        " AND " + Tables.Places.Columns._ID + "=" + uri.getLastPathSegment() +
                        (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case EVENT_LIST_URI_INDICATOR :
                return simpleQuery(Tables.Events.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case EVENT_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.Events.TABLE_NAME, uri, projection,
                        Tables.Events.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case EVENT_TIMETABLE_LIST_URI_INDICATOR :
                return simpleQuery(Tables.EventsTimetable.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case EVENT_TIMETABLE_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.EventsTimetable.TABLE_NAME, uri, projection,
                        Tables.EventsTimetable.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
            case EVENT_TIMETABLE_VIEW_LIST_URI_INDICATOR :
                return simpleQuery(Tables.EventsTimetableView.VIEW_NAME, uri, projection, selection, selectionArgs, sortOrder);

            case COMMENTS_RATING_LIST_URI_INDICATOR :
                return simpleQuery(Tables.CommentsRating.TABLE_NAME, uri, projection, selection, selectionArgs, sortOrder);
            case COMMENTS_RATING_ITEM_URI_INDICATOR :
                return simpleQuery(Tables.CommentsRating.TABLE_NAME, uri, projection,
                        Tables.CommentsRating.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null), sortOrder);
        }

        throw new IllegalArgumentException("Unknown uri for query : " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case FILM_LIST_URI_INDICATOR :
                return simpleInsert(Tables.Films.TABLE_NAME, uri, values);
            case FILM_TIMETABLE_LIST_URI_INDICATOR:
                return simpleInsert(Tables.FilmsTimetable.TABLE_NAME, uri, values);
            case CINEMA_LIST_URI_INDICATOR :
                return simpleInsert(Tables.Cinemas.TABLE_NAME, uri, values);
            case COMMENT_LIST_URI_INDICATOR :
                return simpleInsert(Tables.Comments.TABLE_NAME, uri, values);
            case ANNOUNCEMENTS_LIST_URI_INDICATOR :
                return simpleInsert(Tables.AnnouncementsMetadata.TABLE_NAME, uri, values);
            case PLACE_LIST_URI_INDICATOR :
                values.put(Tables.Places.Columns.PLACE_TYPE, Integer.parseInt(uri.getLastPathSegment()));
                return simpleInsert(Tables.Places.TABLE_NAME, uri, values);
            case EVENT_LIST_URI_INDICATOR :
                return simpleInsert(Tables.Events.TABLE_NAME, uri, values);
            case EVENT_TIMETABLE_LIST_URI_INDICATOR :
                return simpleInsert(Tables.EventsTimetable.TABLE_NAME, uri, values);
            case COMMENTS_RATING_LIST_URI_INDICATOR :
                return simpleInsert(Tables.CommentsRating.TABLE_NAME, uri, values);
        }
        throw new IllegalArgumentException("Unknown uri for insert : " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        boolean hasSelection = !TextUtils.isEmpty(selection);

        switch (uriMatcher.match(uri)) {
            case FILM_LIST_URI_INDICATOR :
                return simpleDelete(Tables.Films.TABLE_NAME, uri, selection, selectionArgs);
            case FILM_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.Films.TABLE_NAME, uri,
                        Tables.Films.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case FILM_TIMETABLE_LIST_URI_INDICATOR:
                return simpleDelete(Tables.FilmsTimetable.TABLE_NAME, uri, selection, selectionArgs);
            case FILM_TIMETABLE_ITEM_URI_INDICATOR:
                return simpleDelete(Tables.FilmsTimetable.TABLE_NAME, uri,
                        Tables.FilmsTimetable.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case CINEMA_LIST_URI_INDICATOR :
                return simpleDelete(Tables.Cinemas.TABLE_NAME, uri, selection, selectionArgs);
            case CINEMA_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.Cinemas.TABLE_NAME, uri,
                        Tables.Cinemas.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case COMMENT_LIST_URI_INDICATOR :
                return simpleDelete(Tables.Comments.TABLE_NAME, uri, selection, selectionArgs);
            case COMMENT_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.Comments.TABLE_NAME, uri,
                        Tables.Comments.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case ANNOUNCEMENTS_LIST_URI_INDICATOR :
                return simpleDelete(Tables.AnnouncementsMetadata.TABLE_NAME, uri, selection, selectionArgs);
            case ANNOUNCEMENTS_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.AnnouncementsMetadata.TABLE_NAME, uri,
                        Tables.AnnouncementsMetadata.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case PLACE_LIST_URI_INDICATOR :
                return simpleDelete(Tables.Places.TABLE_NAME, uri,
                        Tables.Places.Columns.PLACE_TYPE + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case PLACE_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.Places.TABLE_NAME, uri,
                        Tables.Places.Columns.PLACE_TYPE + "=" + uri.getPathSegments().get(1) +
                        " AND " + Tables.Places.Columns._ID + "=" + uri.getLastPathSegment() +
                        (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case EVENT_LIST_URI_INDICATOR :
                return simpleDelete(Tables.Events.TABLE_NAME, uri, selection, selectionArgs);
            case EVENT_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.Events.TABLE_NAME, uri,
                        Tables.Events.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case EVENT_TIMETABLE_LIST_URI_INDICATOR :
                return simpleDelete(Tables.EventsTimetable.TABLE_NAME, uri, selection, selectionArgs);
            case EVENT_TIMETABLE_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.EventsTimetable.TABLE_NAME, uri,
                        Tables.EventsTimetable.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case COMMENTS_RATING_LIST_URI_INDICATOR :
                return simpleDelete(Tables.CommentsRating.TABLE_NAME, uri, selection, selectionArgs);
            case COMMENTS_RATING_ITEM_URI_INDICATOR :
                return simpleDelete(Tables.CommentsRating.TABLE_NAME, uri,
                        Tables.CommentsRating.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
        }

        throw new IllegalArgumentException("Unknown uri for delete : " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        boolean hasSelection = !TextUtils.isEmpty(selection);

        switch (uriMatcher.match(uri)) {
            case FILM_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.Films.TABLE_NAME, uri, values, selection, selectionArgs);
            case FILM_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.Films.TABLE_NAME, uri, values,
                        Tables.Films.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case FILM_TIMETABLE_LIST_URI_INDICATOR:
                return simpleUpdate(Tables.FilmsTimetable.TABLE_NAME, uri, values, selection, selectionArgs);
            case FILM_TIMETABLE_ITEM_URI_INDICATOR:
                return simpleUpdate(Tables.FilmsTimetable.TABLE_NAME, uri, values,
                        Tables.FilmsTimetable.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case CINEMA_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.Cinemas.TABLE_NAME, uri, values, selection, selectionArgs);
            case CINEMA_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.Cinemas.TABLE_NAME, uri, values,
                        Tables.Cinemas.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case COMMENT_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.Comments.TABLE_NAME, uri, values, selection, selectionArgs);
            case COMMENT_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.Comments.TABLE_NAME, uri, values,
                        Tables.Comments.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case ANNOUNCEMENTS_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.AnnouncementsMetadata.TABLE_NAME, uri, values, selection, selectionArgs);
            case ANNOUNCEMENTS_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.AnnouncementsMetadata.TABLE_NAME, uri, values,
                        Tables.AnnouncementsMetadata.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case PLACE_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.Places.TABLE_NAME, uri, values,
                        Tables.Places.Columns.PLACE_TYPE + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case PLACE_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.Places.TABLE_NAME, uri, values,
                        Tables.Places.Columns.PLACE_TYPE + "=" + uri.getPathSegments().get(1) +
                        " AND " + Tables.Places.Columns._ID + "=" + uri.getLastPathSegment() +
                        (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case EVENT_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.Events.TABLE_NAME, uri, values, selection, selectionArgs);
            case EVENT_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.Events.TABLE_NAME, uri, values,
                        Tables.Events.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case EVENT_TIMETABLE_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.EventsTimetable.TABLE_NAME, uri, values, selection, selectionArgs);
            case EVENT_TIMETABLE_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.EventsTimetable.TABLE_NAME, uri, values,
                        Tables.EventsTimetable.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
            case COMMENTS_RATING_LIST_URI_INDICATOR :
                return simpleUpdate(Tables.CommentsRating.TABLE_NAME, uri, values, selection, selectionArgs);
            case COMMENTS_RATING_ITEM_URI_INDICATOR :
                return simpleUpdate(Tables.CommentsRating.TABLE_NAME, uri, values,
                        Tables.CommentsRating.Columns._ID + "=" + uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
        }

        throw new IllegalArgumentException("Unknown uri for update : " + uri);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("TodayProvider doesn't implement getType() method");
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        final SQLiteDatabase db = sqlStorage.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Helper method for selecting all data from table by name
     * @param tableName Name of the table in the database
     * @param uri The URI to query
     * @param projection The list of columns to put into the cursor. If null all columns are included
     * @param selection A selection criteria to apply when filtering rows. If null then all rows are included
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings
     * @param sortOrder How the rows in the cursor should be sorted. If null then the provider is free to define the sort order
     * @return Cursor to the data
     */
    private Cursor simpleQuery(String tableName, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(tableName);

        //make query
        SQLiteDatabase db = sqlStorage.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Helper method for inserting row into the table by the table name
     * @param tableName Name of the table in the database
     * @param uri The URI of the insertion request
     * @param values A set of column_name/value pairs to add to the database. This must not be null.
     * @return The URI for the newly inserted item
     */
    private Uri simpleInsert(String tableName, Uri uri, ContentValues values) {
        SQLiteDatabase db = sqlStorage.getWritableDatabase();

        long rowId = db.insert(tableName, null, values);

        if (rowId > 0)
        {
            Uri resultUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return resultUri;
        }
        throw new SQLiteException("Faild to insert row into " + uri);
    }

    /**
     * Helper method for deleting rows from the table by the table name
     * @param tableName Name of the table in the database
     * @param uri The URI of the deleting request
     * @param selection An optional restriction to apply to rows when deleting.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings
     * @return The number of rows affected
     */
    private int simpleDelete(String tableName, Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = sqlStorage.getWritableDatabase();

        int rowsDeleted = db.delete(tableName, selection, selectionArgs);

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Helper method for updating rows from the table by the table name
     * @param tableName Name of the table in the database
     * @param uri The URI to query
     * @param values A set of column_name/value pairs to update in the database. This must not be null
     * @param selection An optional filter to match rows to update
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings
     * @return The number of rows affected
     */
    private int simpleUpdate(String tableName, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = sqlStorage.getWritableDatabase();
        int rowsUpdated = db.update(tableName, values, selection, selectionArgs);

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
