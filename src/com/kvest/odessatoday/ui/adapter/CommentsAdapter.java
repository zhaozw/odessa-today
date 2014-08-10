package com.kvest.odessatoday.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import com.kvest.odessatoday.R;
import com.kvest.odessatoday.provider.TodayProviderContract;
import com.kvest.odessatoday.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import static com.kvest.odessatoday.provider.TodayProviderContract.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 10.08.14
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
public class CommentsAdapter extends CursorAdapter {
    public static final String[] PROJECTION = new String[]{Tables.Comments.Columns._ID, Tables.Comments.Columns.DATE,
                                                           Tables.Comments.Columns.NAME, Tables.Comments.Columns.TEXT};
    private static final String DATE_FORMAT_PATTERN = " dd.MM.yyyy ";

    private int dateColumnIndex = -1;
    private int nameColumnIndex = -1;
    private int textColumnIndex = -1;

    private SimpleDateFormat dateFormat;

    public CommentsAdapter(Context context) {
        super(context, null, 0);
        dateFormat = new SimpleDateFormat(context.getString(R.string.comments_date_format));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //create view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comments_list_item, viewGroup, false);

        //create holder
        ViewHolder holder = new ViewHolder();
        holder.date = (TextView)view.findViewById(R.id.comment_date);
        holder.name = (TextView)view.findViewById(R.id.comment_author_name);
        holder.text = (TextView)view.findViewById(R.id.comment_text);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        if (!isColumnIndexesCalculated()) {
            calculateColumnIndexes(cursor);
        }

        long dateMillis = TimeUnit.SECONDS.toMillis(cursor.getLong(dateColumnIndex));
        holder.date.setText(dateFormat.format(dateMillis));
        holder.name.setText(Html.fromHtml(cursor.getString(nameColumnIndex)));
        holder.text.setText(Html.fromHtml(cursor.getString(textColumnIndex)));
    }

    private boolean isColumnIndexesCalculated() {
        return (dateColumnIndex >= 0);
    }

    private void calculateColumnIndexes(Cursor cursor) {
        dateColumnIndex = cursor.getColumnIndex(Tables.Comments.Columns.DATE);
        nameColumnIndex = cursor.getColumnIndex(Tables.Comments.Columns.NAME);
        textColumnIndex = cursor.getColumnIndex(Tables.Comments.Columns.TEXT);

    }

    private static class ViewHolder {
        private TextView date;
        private TextView name;
        private TextView text;
    }
}