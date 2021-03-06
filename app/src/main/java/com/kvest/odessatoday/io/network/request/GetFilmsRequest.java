package com.kvest.odessatoday.io.network.request;

import android.net.Uri;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.kvest.odessatoday.io.network.NetworkContract;
import com.kvest.odessatoday.io.network.response.GetFilmsResponse;

import java.io.UnsupportedEncodingException;
/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 03.06.14
 * Time: 23:06
 * To change this template use File | Settings | File Templates.
 */
public class GetFilmsRequest extends BaseRequest<GetFilmsResponse> {
    private long startDate;
    private long endDate;
    private long cinemaId;

    public GetFilmsRequest(long startDate, long endDate, long cinemaId, Response.Listener<GetFilmsResponse> listener,
                           Response.ErrorListener errorListener) {
        super(Method.GET, generateUrl(startDate, endDate, cinemaId), null, listener, errorListener);

        this.startDate = startDate;
        this.endDate = endDate;
        this.cinemaId = cinemaId;
    };

    @Override
    protected Response<GetFilmsResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            //get string response
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            GetFilmsResponse getFilmsResponse = gson.fromJson(json, GetFilmsResponse.class);

            return Response.success(getFilmsResponse, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public long getCinemaId() {
        return cinemaId;
    }

    private static String generateUrl(long startDate, long endDate, long cinemaId) {
        Uri.Builder builder = NetworkContract.FilmsRequest.url.buildUpon();
        if (startDate >= 0) {
            builder.appendQueryParameter(NetworkContract.FilmsRequest.Params.START_DATE, Long.toString(startDate));
        }
        if (endDate >= 0) {
            builder.appendQueryParameter(NetworkContract.FilmsRequest.Params.END_DATE, Long.toString(endDate));
        }
        if (cinemaId >=0) {
            builder.appendQueryParameter(NetworkContract.FilmsRequest.Params.CINEMA_ID, Long.toString(cinemaId));
        }

        return builder.build().toString();
    }
}
