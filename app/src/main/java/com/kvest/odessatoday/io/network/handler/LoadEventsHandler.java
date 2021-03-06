package com.kvest.odessatoday.io.network.handler;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.android.volley.toolbox.RequestFuture;
import com.kvest.odessatoday.TodayApplication;
import com.kvest.odessatoday.datamodel.Event;
import com.kvest.odessatoday.io.network.event.EventsLoadedEvent;
import com.kvest.odessatoday.io.network.request.GetEventsRequest;
import com.kvest.odessatoday.io.network.response.GetEventsResponse;
import com.kvest.odessatoday.provider.TodayProviderContract;
import com.kvest.odessatoday.utils.BusProvider;
import com.kvest.odessatoday.utils.Constants;
import com.kvest.odessatoday.utils.SelectionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.kvest.odessatoday.provider.TodayProviderContract.CONTENT_AUTHORITY;
import static com.kvest.odessatoday.provider.TodayProviderContract.EVENTS_URI;
import static com.kvest.odessatoday.provider.TodayProviderContract.EVENTS_TIMETABLE_URI;
import static com.kvest.odessatoday.utils.LogUtils.LOGE;

/**
 * Created by kvest on 30.11.15.
 */
public class LoadEventsHandler extends RequestHandler {
    private static final long EMPTY_PLACE_ID = -1L;
    private static final int EMPTY_TYPE = -1;
    private static final String EXTRA_START_DATE = "com.kvest.odessatoday.EXTRAS.START_DATE";
    private static final String EXTRA_END_DATE = "com.kvest.odessatoday.EXTRAS.END_DATE";
    private static final String EXTRA_PLACE_ID = "com.kvest.odessatoday.EXTRAS.PLACE_ID";
    private static final String EXTRA_TYPE = "com.kvest.odessatoday.EXTRAS.TYPE";

    public static void putExtras(Intent intent, long startDate, long endDate, long placeId) {
        intent.putExtra(EXTRA_START_DATE, startDate);
        intent.putExtra(EXTRA_END_DATE, endDate);
        intent.putExtra(EXTRA_PLACE_ID, placeId);
    }

    public static void putExtras(Intent intent, long startDate, long endDate, int type) {
        intent.putExtra(EXTRA_START_DATE, startDate);
        intent.putExtra(EXTRA_END_DATE, endDate);
        intent.putExtra(EXTRA_TYPE, type);
    }

    @Override
    public void processIntent(Context context, Intent intent) {
        //get extra data
        long startDate = intent.getLongExtra(EXTRA_START_DATE, -1);
        long endDate = intent.getLongExtra(EXTRA_END_DATE, -1);
        long placeId = intent.getLongExtra(EXTRA_PLACE_ID, EMPTY_PLACE_ID);
        int type = intent.getIntExtra(EXTRA_TYPE, EMPTY_TYPE);

        //send request
        RequestFuture<GetEventsResponse> future = RequestFuture.newFuture();
        GetEventsRequest request;
        if (type == EMPTY_TYPE) {
            request = new GetEventsRequest(startDate, endDate, placeId, future, future);
        } else {
            request = new GetEventsRequest(startDate, endDate, type, future, future);
        }
        TodayApplication.getApplication().getVolleyHelper().addRequest(request);
        try {
            GetEventsResponse response = future.get();
            if (response.isSuccessful()) {
                //save events
                saveEvents(context, response.data.events, request.getStartDate(), request.getEndDate(), request.getPlaceId(), request.getType());

                //notify listeners about successful loading events
                int eventsCount = response.data.events != null ? response.data.events.size() : 0;
                BusProvider.getInstance().post(new EventsLoadedEvent(type, placeId, true, eventsCount));
            } else {
                LOGE(Constants.TAG, "ERROR " + response.code + " = " + response.error);

                //notify listeners about unsuccessful loading events
                BusProvider.getInstance().post(new EventsLoadedEvent(type, placeId, false, 0));
            }
        } catch (InterruptedException e) {
            LOGE(Constants.TAG, e.getLocalizedMessage());

            //notify listeners about unsuccessful loading events
            BusProvider.getInstance().post(new EventsLoadedEvent(type, placeId, false, 0));
        } catch (ExecutionException e) {
            LOGE(Constants.TAG, e.getLocalizedMessage());

            //notify listeners about unsuccessful loading events
            BusProvider.getInstance().post(new EventsLoadedEvent(type, placeId, false, 0));
        }
    }

    private void saveEvents(Context context, List<Event> events, long startDate, long endDate, long placeId, int type) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        //delete timetable from startDate to endDate
        SelectionBuilder selectionBuilder = new SelectionBuilder();
        if (startDate >= 0) {
            selectionBuilder.and(TodayProviderContract.Tables.EventsTimetable.Columns.DATE + ">=?", Long.toString(startDate));
        }
        if (endDate >= 0) {
            selectionBuilder.and(TodayProviderContract.Tables.EventsTimetable.Columns.DATE + "<=?", Long.toString(endDate));
        }
        if (placeId != EMPTY_PLACE_ID) {
            selectionBuilder.and(TodayProviderContract.Tables.EventsTimetable.Columns.PLACE_ID + "=?", Long.toString(placeId));
        }
        if (type != EMPTY_TYPE) {
            selectionBuilder.and(TodayProviderContract.Tables.EventsTimetable.Columns.EVENT_ID + " in (" +
                                 TodayProviderContract.Tables.Events.GET_EVENTS_ID_BY_TYPE_SQL + ")",
                                 Integer.toString(type));
        }
        ContentProviderOperation deleteOperation = ContentProviderOperation.newDelete(EVENTS_TIMETABLE_URI)
                .withSelection(selectionBuilder.getSelection(), selectionBuilder.getSelectionArgs())
                .build();
        operations.add(deleteOperation);

        if (events != null) {
            for (Event event : events) {
                //insert event
                operations.add(ContentProviderOperation.newInsert(EVENTS_URI).withValues(event.getContentValues()).build());

                //insert timetable
                for (Event.Timetable timetableItem : event.timetable) {
                    operations.add(ContentProviderOperation.newInsert(EVENTS_TIMETABLE_URI).withValues(timetableItem.getContentValues(event.id)).build());
                }
            }
        }

        //apply
        try {
            context.getContentResolver().applyBatch(CONTENT_AUTHORITY, operations);
        }catch (RemoteException re) {
            LOGE(Constants.TAG, re.getMessage());
        }catch (OperationApplicationException oae) {
            LOGE(Constants.TAG, oae.getMessage());
        }
    }
}
