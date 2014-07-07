package com.kvest.odessatoday.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 30.05.14
 * Time: 22:08
 * To change this template use File | Settings | File Templates.
 */
public class TodayProviderContract {
    public static final String CONTENT_AUTHORITY = "com.kvest.odessatoday";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String FILMS_PATH = "films";
    static final String TIMETABLE_PATH = "timetable";
    static final String CINEMAS_PATH = "cinemas";

    public static final Uri FILMS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, FILMS_PATH);
    public static final Uri TIMETABLE_URI = Uri.withAppendedPath(BASE_CONTENT_URI, FILMS_PATH + "/" + TIMETABLE_PATH);

    public interface Tables {
        interface Films {
            String TABLE_NAME = "films";

            interface Columns extends BaseColumns {
                String FILM_ID = "film_id";
                String NAME = "name";
                String COUNTRY = "country";
                String YEAR = "year";
                String DIRECTOR = "director";
                String ACTORS = "actors";
                String DESCRIPTION = "description";
                String IMAGE = "image";
                String VIDEO = "video";
                String GENRE = "genre";
                String RATING = "rating";
                String COMMENTS_COUNT = "comments_count";
                String IS_PREMIERE = "is_premiere";
            }

            String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Columns.FILM_ID + " INTEGER,"
                    + Columns.NAME + " TEXT,"
                    + Columns.COUNTRY + " TEXT,"
                    + Columns.YEAR + " INTEGER,"
                    + Columns.DIRECTOR + " TEXT, "
                    + Columns.ACTORS + " TEXT, "
                    + Columns.DESCRIPTION + " TEXT, "
                    + Columns.IMAGE + " TEXT, "
                    + Columns.VIDEO + " TEXT, "
                    + Columns.GENRE + " TEXT, "
                    + Columns.RATING + " REAL, "
                    + Columns.COMMENTS_COUNT + " INTEGER DEFAULT 0, "
                    + Columns.IS_PREMIERE + " INTEGER DEFAULT 0, "
                    + "UNIQUE (" + Columns.FILM_ID + ") ON CONFLICT REPLACE)";

            String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
        }

        interface FilmsTimetable {
            String TABLE_NAME = "films_timetable";

            interface Columns extends BaseColumns {
                String TIMETABLE_ID = "timetable_id";
                String FILM_ID = "film_id";
                String CINEMA_ID = "cinema_id";
                String DATE = "date";
                String PRICES = "prices";
                String FORMAT = "format";
            }

            String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Columns.TIMETABLE_ID + " INTEGER,"
                    + Columns.FILM_ID + " INTEGER,"
                    + Columns.CINEMA_ID + " INTEGER,"
                    + Columns.DATE + " INTEGER,"
                    + Columns.PRICES + " TEXT,"
                    + Columns.FORMAT + " INTEGER, "
                    + "UNIQUE(" + Columns.TIMETABLE_ID + ") ON CONFLICT REPLACE);";
//                    + "FOREIGN KEY(" + Columns.FILM_ID + ") REFERENCES " + Films.TABLE_NAME + "(" + Films.Columns.FILM_ID + ") ON UPDATE NO ACTION ON DELETE CASCADE);";

            String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
            String GET_FILMS_ID_BY_PERIOD_SQL = "SELECT DISTINCT " + Columns.FILM_ID + " FROM " + TABLE_NAME +
                                                " WHERE " + Columns.DATE + ">=? AND " + Columns.DATE + "<=?";
        }

        interface Cinemas {
            String TABLE_NAME = "cinemas";
            interface Columns extends BaseColumns {
                String CINEMA_ID = "cinema_id";
                String NAME = "name";
                String ADDRESS = "address";
                String PHONES = "phones";
                String TRANSPORT = "transport";
                String DESCRIPTION = "description";
                String WORK_TIME = "worktime";
                String IMAGE = "image";
            }

            String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Columns.CINEMA_ID + " INTEGER,"
                    + Columns.NAME + " TEXT,"
                    + Columns.ADDRESS + " TEXT,"
                    + Columns.PHONES + " TEXT,"
                    + Columns.TRANSPORT + " TEXT,"
                    + Columns.DESCRIPTION + " TEXT,"
                    + Columns.WORK_TIME + " TEXT,"
                    + Columns.IMAGE + " TEXT,"
                    + "UNIQUE(" + Columns.CINEMA_ID + ") ON CONFLICT REPLACE);";

            String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;
        }
    }
}
