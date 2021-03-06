package com.kvest.odessatoday.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.kvest.odessatoday.R;
import com.kvest.odessatoday.io.network.event.FilmsLoadedEvent;
import com.kvest.odessatoday.provider.DataProviderHelper;
import com.kvest.odessatoday.service.NetworkService;
import com.kvest.odessatoday.ui.activity.FilmDetailsActivity;
import com.kvest.odessatoday.ui.adapter.CinemaTimetableAdapter;
import com.kvest.odessatoday.ui.widget.CommentsCountView;
import com.kvest.odessatoday.utils.BusProvider;
import com.kvest.odessatoday.utils.Constants;
import com.kvest.odessatoday.utils.TimeUtils;
import com.kvest.odessatoday.utils.Utils;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import static com.kvest.odessatoday.provider.TodayProviderContract.*;
import static com.kvest.odessatoday.utils.LogUtils.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 04.11.14
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
public class CinemaDetailsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARGUMENT_CINEMA_ID = "com.kvest.odessatoday.argument.CINEMA_ID";

    private static final String DATE_FORMAT_PATTERN = " dd MMMM yyyy, cccc";
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    private static final int CINEMA_LOADER_ID = 1;
    private static final int TIMETABLE_LOADER_ID = 2;

    private TextView cinemaName;
    private TextView phones;
    private TextView address;
    private TextView description;
    private CommentsCountView actionCommentsCount;
    private ListView timetableList;
    private String[] photoUrls = null;
    private double latitude = 0d;
    private double longitude = 0d;
    private float rating;

    private View noFilmsLabel;

    private int drawablesColor;

    private TextView dateTextView;
    private long shownDate;

    private CinemaTimetableAdapter cinemaTimetableAdapter;

    private CinemaDetailsActionsListener cinemaDetailsActionsListener;

    public static CinemaDetailsFragment newInstance(long cinemaId) {
        Bundle arguments = new Bundle(1);
        arguments.putLong(ARGUMENT_CINEMA_ID, cinemaId);

        CinemaDetailsFragment result = new CinemaDetailsFragment();
        result.setArguments(arguments);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shownDate = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.cinema_details_fragment, container, false);
        View headerView = inflater.inflate(R.layout.cinema_details_header, null);

        init(rootView, headerView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BusProvider.getInstance().unregister(this);
    }

    private void init(View rootView, View headerView) {
        initResources(getActivity());

        cinemaName = (TextView) headerView.findViewById(R.id.cinema_name);
        phones = (TextView) headerView.findViewById(R.id.cinema_phones);
        address = (TextView) headerView.findViewById(R.id.cinema_address);
        description = (TextView) headerView.findViewById(R.id.description);
        actionCommentsCount = (CommentsCountView) headerView.findViewById(R.id.action_comments_count);
        dateTextView = (TextView)headerView.findViewById(R.id.date);

        noFilmsLabel = headerView.findViewById(R.id.no_films);

        //colorize drawables
        Utils.setDrawablesColor(drawablesColor, phones.getCompoundDrawables());
        Utils.setDrawablesColor(drawablesColor, address.getCompoundDrawables());

        //set shown date UI
        updateShownDateUI();

        //setup timetable list
        timetableList = (ListView)rootView.findViewById(R.id.cinema_details_list);
        timetableList.addHeaderView(headerView, null, false);
        cinemaTimetableAdapter = new CinemaTimetableAdapter(getActivity());
        timetableList.setAdapter(cinemaTimetableAdapter);

        timetableList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showFilm(id);
            }
        });
        headerView.findViewById(R.id.action_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCinemaOnMap();
            }
        });
        headerView.findViewById(R.id.action_comments_count).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showComments();
            }
        });
        headerView.findViewById(R.id.action_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotos();
            }
        });

        headerView.findViewById(R.id.show_calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendar();
            }
        });
    }

    private void initResources(Context context) {
        // The attributes you want retrieved
        int[] attrs = {R.attr.CinemaDetailsDrawablesColor};

        // Parse style, using Context.obtainStyledAttributes()
        TypedArray ta = context.obtainStyledAttributes(attrs);

        try {
            drawablesColor = ta.getColor(ta.getIndex(0), Color.BLACK);
        } finally {
            ta.recycle();
        }
    }

    public void setShownDate(long shownDate) {
        this.shownDate = shownDate;

        updateShownDateUI();

        //start loading data
        NetworkService.loadFilms(getActivity(), shownDate, TimeUtils.getEndOfTheDay(shownDate), getCinemaId());

        //restart loader
        getLoaderManager().restartLoader(TIMETABLE_LOADER_ID, null, this);
    }

    private void showFilm(long filmId) {
        FilmDetailsActivity.startClearTop(getActivity(), filmId, shownDate);
    }

    private void updateShownDateUI() {
        String dateValue = (TimeUtils.isCurrentDay(shownDate) ? getString(R.string.today_marker) : "") +
                            DATE_FORMAT.format(TimeUnit.SECONDS.toMillis(shownDate));
        dateTextView.setText(dateValue);
    }

    private void showCalendar() {
        if (cinemaDetailsActionsListener != null) {
            cinemaDetailsActionsListener.onShowCalendar(shownDate);
        }
    }

    private void showPhotos() {
        if (cinemaDetailsActionsListener != null && photoUrls != null) {
            cinemaDetailsActionsListener.onShowCinemaPhotos(photoUrls, cinemaName.getText().toString(), getCinemaId());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            cinemaDetailsActionsListener = (CinemaDetailsActionsListener) activity;
        } catch (ClassCastException cce) {
            LOGE(Constants.TAG, "Host activity for CinemaDetailsFragment should implements CinemaDetailsFragment.CinemaDetailsActionsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        cinemaDetailsActionsListener = null;
    }

    private void showComments() {
        if (cinemaDetailsActionsListener != null) {
            cinemaDetailsActionsListener.onShowCinemaComments(getCinemaId(), cinemaName.getText().toString(),
                                                              actionCommentsCount.getCommentsCount(), rating);
        }
    }

    private void showCinemaOnMap() {
        Uri geoLocation = Uri.parse("geo:0,0?q=" + latitude + "," +longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.map_not_found, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Request full timetable for the cinema for today
        NetworkService.loadFilms(getActivity(), shownDate, TimeUtils.getEndOfTheDay(shownDate), getCinemaId());

        getLoaderManager().initLoader(CINEMA_LOADER_ID, null, this);
        getLoaderManager().initLoader(TIMETABLE_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CINEMA_LOADER_ID :
                return DataProviderHelper.getCinemaLoader(getActivity(), getCinemaId(), null);
            case TIMETABLE_LOADER_ID :
                long endDate = TimeUtils.getEndOfTheDay(shownDate);
                return DataProviderHelper.getCinemaTimetableLoader(getActivity(), getCinemaId(), shownDate, endDate,
                        CinemaTimetableAdapter.PROJECTION , Tables.CinemaTimetableView.ORDER_FILM_ASC_DATE_ASC);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CINEMA_LOADER_ID :
                setCinemaData(data);
                break;
            case TIMETABLE_LOADER_ID :
                cinemaTimetableAdapter.setCursor(data);
                if (data.getCount() > 0) {
                    noFilmsLabel.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TIMETABLE_LOADER_ID :
                cinemaTimetableAdapter.setCursor(null);
                break;
        }
    }

    private long getCinemaId() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            return arguments.getLong(ARGUMENT_CINEMA_ID, -1);
        } else {
            return -1;
        }
    }

    private void setCinemaData(Cursor cursor) {
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            cinemaName.setText(cursor.getString(cursor.getColumnIndex(Tables.Cinemas.Columns.NAME)));

            String tmp = cursor.getString(cursor.getColumnIndex(Tables.Cinemas.Columns.PHONES));
            if (TextUtils.isEmpty(tmp)) {
                phones.setVisibility(View.GONE);
            } else {
                phones.setVisibility(View.VISIBLE);
                phones.setText(Html.fromHtml(tmp));
            }

            String addressValue = cursor.getString(cursor.getColumnIndex(Tables.Cinemas.Columns.ADDRESS));
            if (TextUtils.isEmpty(addressValue)) {
                address.setVisibility(View.GONE);
            } else {
                address.setVisibility(View.VISIBLE);
                address.setText(addressValue);
            }

            int commentsCount = cursor.getInt(cursor.getColumnIndex(Tables.Cinemas.Columns.COMMENTS_COUNT));
            actionCommentsCount.setCommentsCount(commentsCount);

            String descriptionValue = cursor.getString(cursor.getColumnIndex(Tables.Cinemas.Columns.DESCRIPTION));
            if (TextUtils.isEmpty(descriptionValue)) {
                description.setVisibility(View.GONE);
            } else {
                description.setVisibility(View.VISIBLE);
                description.setText(descriptionValue);
            }

            tmp = cursor.getString(cursor.getColumnIndex(Tables.Cinemas.Columns.IMAGE));
            photoUrls = tmp != null ? Utils.string2Images(tmp) : null;

            //remember geo location
            longitude = cursor.getDouble(cursor.getColumnIndex(Tables.Cinemas.Columns.LON));
            latitude = cursor.getDouble(cursor.getColumnIndex(Tables.Cinemas.Columns.LAT));

            rating = cursor.getFloat(cursor.getColumnIndex(Tables.Cinemas.Columns.RATING));
        }
    }

    @Subscribe
    public void onFilmsLoaded(FilmsLoadedEvent event) {
        Activity activity = getActivity();
        if (!event.isSuccessful() && activity != null) {
            showErrorSnackbar(activity, R.string.error_loading_films);
        }

        //check films exists
        if (event.isSuccessful() && event.startDate == shownDate && event.filmsCount == 0) {
            noFilmsLabel.post(new Runnable() {
                @Override
                public void run() {
                    noFilmsLabel.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public interface CinemaDetailsActionsListener {
        void onShowCinemaComments(long cinemaId, String cinemaName, int commentsCount, float rating);
        void onShowCinemaPhotos(String[] photoURLs, String cinemaName, long cinemaId);
        void onShowCalendar(long withDate);
    }
}
