package com.kvest.odessatoday.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.kvest.odessatoday.R;
import com.kvest.odessatoday.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.kvest.odessatoday.utils.LogUtils.*;
/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 08.07.14
 * Time: 21:03
 * To change this template use File | Settings | File Templates.
 */
public class CalendarFragment extends Fragment {
    private static final String ARGUMENT_SELECTED_DATE_DAY = "com.kvest.odessatoday.argument.SELECTED_DATE_DAY";
    private static final String ARGUMENT_SELECTED_DATE_MONTH = "com.kvest.odessatoday.argument.SELECTED_DATE_MONTH";
    private static final String ARGUMENT_SELECTED_DATE_YEAR = "com.kvest.odessatoday.argument.SELECTED_DATE_YEAR";
    private static final String DATE_FORMAT_PATTERN = "LLLL yyyy";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    private Calendar calendar;
    private CalendarAdapter adapter;
    private int shownMonthNumber;
    private int currentMonthNumber;
    private TextView shownMonthLabel;
    private Button showPreviousMonthButton;

    private OnDateSelectedListener onDateSelectedListener;

    public static CalendarFragment getInstance(int day, int month, int year) {
        Bundle arguments = new Bundle(3);
        arguments.putInt(ARGUMENT_SELECTED_DATE_DAY, day);
        arguments.putInt(ARGUMENT_SELECTED_DATE_MONTH, month);
        arguments.putInt(ARGUMENT_SELECTED_DATE_YEAR, year);

        CalendarFragment result = new CalendarFragment();
        result.setArguments(arguments);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.calendar_fragment, container, false);

        init(root);

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onDateSelectedListener = (OnDateSelectedListener) activity;
        } catch (ClassCastException cce) {
            LOGE(Constants.TAG, "Host activity for CalendarFragment should implements CalendarFragment.OnDateSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onDateSelectedListener = null;
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        this.onDateSelectedListener = onDateSelectedListener;
    }

    private void init(View root) {
        calendar = Calendar.getInstance();
        currentMonthNumber = getMonthNumber(System.currentTimeMillis());

        shownMonthLabel = (TextView) root.findViewById(R.id.shown_month_label);

        //setup adapter
        adapter = new CalendarAdapter(root.getContext());
        GridView daysGridView = (GridView)root.findViewById(R.id.calendar_grid);
        daysGridView.setAdapter(adapter);
        daysGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CalendarDay calendarDay = (CalendarDay)adapter.getItem(position);
                if ((calendarDay.type == CalendarDay.DAY_TYPE_ACTIVE || calendarDay.type == CalendarDay.DAY_TYPE_SELECTED)
                     && onDateSelectedListener != null) {
                    onDateSelectedListener.onDateSelected(calendarDay.day, calendarDay.month, calendarDay.year);
                }
            }
        });

        //setup buttons
        root.findViewById(R.id.next_month).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextMonth();
            }
        });
        showPreviousMonthButton = (Button) root.findViewById(R.id.previous_month);
        showPreviousMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviousMonth();
            }
        });

        //show calendar starting from the month with selected day
        CalendarDay selectedDate = getSelectedDate();
        showMonth(calculateMonthNumber(selectedDate.year, selectedDate.month));
    }

    private void showNextMonth() {
        //increment month
        ++shownMonthNumber;

        showMonth(shownMonthNumber);
    }

    private void showPreviousMonth(){
        //decrement month
        --shownMonthNumber;

        showMonth(shownMonthNumber);
    }

    private boolean canShowPreviousMonth() {
        return ((shownMonthNumber - 1) >= currentMonthNumber);
    }

    private CalendarDay getSelectedDate() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            return new CalendarDay(arguments.getInt(ARGUMENT_SELECTED_DATE_DAY),
                                   arguments.getInt(ARGUMENT_SELECTED_DATE_MONTH),
                                   arguments.getInt(ARGUMENT_SELECTED_DATE_YEAR),
                                   CalendarDay.DAY_TYPE_SELECTED);
        } else {
            return new CalendarDay(0,0,0, CalendarDay.DAY_TYPE_SELECTED);
        }
    }

    private void showMonth(int monthNumber) {
        //store month number
        shownMonthNumber = monthNumber;

        //convert month number to the date
        long monthDate = getDateByMonthNumber(monthNumber);

        adapter.setContent(monthDate, getSelectedDate());

        shownMonthLabel.setText(DATE_FORMAT.format(monthDate));

        //set previous button availability
        showPreviousMonthButton.setEnabled(canShowPreviousMonth());
    }

    /**
     * Method returns month number
     * @param date Date to convert
     * @return Month number since 0 year
     */
    private int getMonthNumber(long date) {
        calendar.setTimeInMillis(date);

        return calculateMonthNumber(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    /**
     * Method calculates month number
     * @param year Target date year
     * @param month Target date month
     * @return Month number
     */
    private int calculateMonthNumber(int year, int month) {
        return year * 12 + month;
    }

    private long getDateByMonthNumber(int monthNumber) {
        calendar.clear();
        calendar.set(Calendar.YEAR, monthNumber / 12);
        calendar.set(Calendar.MONTH, monthNumber % 12);

        return calendar.getTimeInMillis();
    }

    public interface OnDateSelectedListener {
        public void onDateSelected(int day, int month, int year);
    }

    private static class CalendarDay {
        public static final int DAY_TYPE_ACTIVE = 0;
        public static final int DAY_TYPE_PASSED = 1;
        public static final int DAY_TYPE_ANOTHER_MONTH = 2;
        public static final int DAY_TYPE_SELECTED = 3;

        public int day;
        public int month;
        public int year;
        public int type;

        public CalendarDay(int day, int month, int year, int type) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.type = type;
        }

        public CalendarDay(Calendar calendar, int type) {
            this.day = calendar.get(Calendar.DAY_OF_MONTH);
            this.month = calendar.get(Calendar.MONTH);
            this.year = calendar.get(Calendar.YEAR);
            this.type = type;
        }
    }

    private static class CalendarAdapter extends BaseAdapter {
        private static final float DAY_TEXT_SIZE = 18f;

        private Context context;
        private List<CalendarDay> days;
        private Calendar calendar;
        private int otherMonthTextColor;
        private int passedDaysTextColor;
        private int activeDaysTextColor;

        public CalendarAdapter(Context context) {
            super();

            this.context = context;
            days = new ArrayList<CalendarDay>();
            calendar = Calendar.getInstance();

            otherMonthTextColor = context.getResources().getColor(R.color.calendar_other_month_text_color);
            passedDaysTextColor = context.getResources().getColor(R.color.calendar_passed_days_text_color);
            activeDaysTextColor = context.getResources().getColor(R.color.calendar_active_days_text_color);
        }

        public void setContent(long monthToShow, CalendarDay selectedDate)  {
            days.clear();

            calendar.setTimeInMillis(monthToShow);

            //get current month
            int month = calendar.get(Calendar.MONTH);

            //Go to first day in month
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            //go to monday and get days in this week from previous month
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            while (calendar.get(Calendar.MONTH) != month) {
                days.add(new CalendarDay(calendar, CalendarDay.DAY_TYPE_ANOTHER_MONTH));

                //go to next day
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            int type;
            Date currentDate = getToday();
            while (calendar.get(Calendar.MONTH) == month) {
                if (calendar.get(Calendar.DAY_OF_MONTH) == selectedDate.day &&
                        calendar.get(Calendar.MONTH) == selectedDate.month &&
                        calendar.get(Calendar.YEAR) == selectedDate.year) {
                    type = CalendarDay.DAY_TYPE_SELECTED;
                } else if (currentDate.after(calendar.getTime())) {
                    type = CalendarDay.DAY_TYPE_PASSED;
                } else {
                    type = CalendarDay.DAY_TYPE_ACTIVE;
                }
                days.add(new CalendarDay(calendar, type));

                //go to the next day
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            //part of the next month
            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                days.add(new CalendarDay(calendar, CalendarDay.DAY_TYPE_ANOTHER_MONTH));

                //go to nest day
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public Object getItem(int position) {
            return days.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = buildView();
            }

            CalendarDay calendarDay = days.get(position);
            //set bg color
            if (calendarDay.type == CalendarDay.DAY_TYPE_SELECTED) {
                convertView.setBackgroundResource(R.drawable.calendar_selected_day_bg);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            //set text color
            switch (calendarDay.type) {
                case CalendarDay.DAY_TYPE_ACTIVE :
                case CalendarDay.DAY_TYPE_SELECTED :
                    ((TextView)convertView).setTextColor(activeDaysTextColor);
                    break;
                case CalendarDay.DAY_TYPE_PASSED :
                    ((TextView)convertView).setTextColor(passedDaysTextColor);
                    break;
                case CalendarDay.DAY_TYPE_ANOTHER_MONTH :
                    ((TextView)convertView).setTextColor(otherMonthTextColor);
                    break;
            }
            //set text
            ((TextView)convertView).setText(Integer.toString(calendarDay.day));

            return convertView;
        }

        private View buildView() {
            TextView view = new TextView(context);
            view.setTypeface(Typeface.create(view.getTypeface(), Typeface.BOLD));
            view.setTextSize(DAY_TEXT_SIZE);
            view.setGravity(Gravity.CENTER);

            return view;
        }

        private static Date getToday() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return new Date(calendar.getTimeInMillis());
        }
    }
}
