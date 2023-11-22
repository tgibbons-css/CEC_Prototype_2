package css.cecprototype2.analysis_logic;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 *  This code writes to the Google sheet at https://docs.google.com/spreadsheets/d/1ddKMcrmgoL6ImzSS3A5NXnaVW5gmLX7j2gk90CX3QOs/edit?usp=sharing
 *
 *  It uses a Apps Script named Chem Sheet Writer. The script assumes the HTTP post contains an action paramters that
 *     must be either "calibrate" or "analysis"
 *
 */
public class SheetWriter {

    Context context;        // we need the app context to write the Volley HTTP post

    public SheetWriter(Context context) {
        this.context = context;
    }

    public void writeCalibrationToSheets(ArrayList<Double> calibrateValues) {
        // TODO: the url should be stored in the string.xml file
        String url = "https://script.google.com/macros/s/AKfycbzSal1-D3ElMlG0uqULb8xbK9_CD8piExocl2MPUlUe63TWgwYjOqkri5LtaQVNMX4Asw/exec";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("CIS 4444", "HTTP Response Recieved: " + response);
                }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("CIS 4444", "HTTP Error Recieved: " + error);
                }
                }
            )
            {
                @Override
                protected Map<String, String> getParams() {
                    Log.d("CIS 4444", "Params being set");
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("action", "calibrate");
                    params.put("date", "today");
                    params.put("c1", calibrateValues.get(0).toString());
                    params.put("c2", calibrateValues.get(1).toString());
                    params.put("c3", calibrateValues.get(2).toString());
                    params.put("c4", calibrateValues.get(3).toString());
                    params.put("c5", calibrateValues.get(4).toString());
                    params.put("c6", calibrateValues.get(5).toString());
                    return params;
                }
        };

        // Instantiate the RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    // TODO: Add method to post Analysis data to Google sheet.

}
