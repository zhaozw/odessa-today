package com.kvest.odessatoday.io.network;

import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 03.06.14
 * Time: 23:08
 * To change this template use File | Settings | File Templates.
 */
public class NetworkContract {
    private static final String BASE_URL = "http://today.od.ua/api/1.0/";
    private static final String FILMS_PATH = "films";
    private static final String CINEMAS_PATH = "cinemas";
    private static final String TIMETABLE_PATH = "timetable";
    private static final String COMMENTS_PATH = "comments";
    private static final String ANNOUNCEMENTS_PATH = "announcement";
    private static final String PLACES_PATH = "places";
    private static final String EVENTS_PATH = "events";
    private static final String GALLERY_PATH = "gallery";
    private static final String TICKETS_PATH = "tickets";
    private static final String ORDER_PATH = "order";

    public static final String API_KEY_HEADER_NAME = "api-key";
    public static final String CLIENT_ID_HEADER_NAME = "Client-Id";

    public interface FilmsRequest {
        Uri url = Uri.parse(BASE_URL + FILMS_PATH);

        public interface Params {
            String START_DATE = "start_date";
            String END_DATE = "end_date";
            String CINEMA_ID = "cinema_id";
        }
    }

    public interface AnnouncementRequest {
        Uri url = Uri.parse(BASE_URL + FILMS_PATH + "/" + ANNOUNCEMENTS_PATH);

        public interface Params {
            String OFFSET = "offset";
            String LIMIT = "limit";
            String ORDER = "order";
        }

        public static final String ORDER_ASC = "asc";
        public static final String ORDER_DESC = "desc";
    }

    public interface CinemasRequest {
        Uri url = Uri.parse(BASE_URL + CINEMAS_PATH);
    }

    public static Uri createTimetableUri(long filmId) {
        return Uri.parse(BASE_URL + FILMS_PATH + "/" + Long.toString(filmId) + "/" + TIMETABLE_PATH);
    }

    public static Uri createFilmCommentsUri(long filmId) {
        return Uri.parse(BASE_URL + FILMS_PATH + "/" + Long.toString(filmId) + "/" + COMMENTS_PATH);
    }

    public static Uri createCinemaCommentsUri(long cinemaId) {
        return Uri.parse(BASE_URL + CINEMAS_PATH + "/" + Long.toString(cinemaId) + "/" + COMMENTS_PATH);
    }

    public static Uri createEventCommentsUri(long eventId) {
        return Uri.parse(BASE_URL + EVENTS_PATH + "/" + Long.toString(eventId) + "/" + COMMENTS_PATH);
    }

    public static Uri createPlaceCommentsUri(long placeId) {
        return Uri.parse(BASE_URL + PLACES_PATH + "/" + Long.toString(placeId) + "/" + COMMENTS_PATH);
    }

    public static Uri createCinemaGalleryUri(long cinemaId) {
        return Uri.parse(BASE_URL + CINEMAS_PATH + "/" + Long.toString(cinemaId) + "/" + GALLERY_PATH);
    }

    public static Uri createPlaceGalleryUri(long placeId) {
        return Uri.parse(BASE_URL + PLACES_PATH + "/" + Long.toString(placeId) + "/" + GALLERY_PATH);
    }

    public static Uri createEventTicketsUri(long eventId) {
        return Uri.parse(BASE_URL + EVENTS_PATH + "/" + Long.toString(eventId) + "/" + TICKETS_PATH);
    }

    public static Uri createEventOrderTicketsUri(long eventId) {
        return Uri.parse(BASE_URL + EVENTS_PATH + "/" + Long.toString(eventId) + "/" + ORDER_PATH);
    }

    public interface CommentsRequest {
        int DEFAULT_OFFSET = 0;
        int MAX_LIMIT = 100;

        public interface Params {
            String OFFSET = "offset";
            String LIMIT = "limit";
        }
    }

    public interface PlacesRequest {
        Uri url = Uri.parse(BASE_URL + PLACES_PATH);

        int DEFAULT_OFFSET = 0;
        int MAX_LIMIT = 100;

        public interface Params {
            String TYPE = "type";
            String OFFSET = "offset";
            String LIMIT = "limit";
        }
    }

    public interface EventsRequest {
        Uri url = Uri.parse(BASE_URL + EVENTS_PATH);

        interface Params {
            String START_DATE = "start_date";
            String END_DATE = "end_date";
            String PLACE_ID = "place_id";
            String TYPE = "type";
        }
    }

    public interface UploadPhotoRequest {
        interface Params {
            String PHOTOS_ARRAY = "photos[]";
        }
    }

    public interface OrderTicketsRequest {
        interface Params {
            String NAME = "name";
            String PHONE = "phone";
            String SECTOR = "sector";
            String AMOUNT = "amount";
        }
    }
}
