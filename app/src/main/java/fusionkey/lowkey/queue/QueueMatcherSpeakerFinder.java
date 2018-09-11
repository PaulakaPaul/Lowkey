package fusionkey.lowkey.queue;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static fusionkey.lowkey.LowKeyApplication.requestQueueSingleton;

/**
 * @author Iusztin Paul
 * @version 1.0
 * @since 31.07.2018
 * <p>
 * <h1>Class that wraps the Queue API hosted in AWS</h1>
 * <h2>It uses lambda functions and Redis ElastiCache as backend</h2>
 * <p>
 * <p>The requests are make with Volley</p>
 */

public class QueueMatcherSpeakerFinder extends QueueMatcherUtils implements IQueueMatcher {

    public QueueMatcherSpeakerFinder(String currentUser, Activity currentActivity) {
        super(currentUser, currentActivity);
    }

    /**
     * This method first makes a POST to add the listener to the queue and create a new lobby.
     * After it waits for speakers to come to the lobby checking with a GET method in the aws cache.
     * When the lobby it's full the speakers are returned in a container hosted by the
     * LobbyCheckerRunnable class that wraps the request and response.
     */
    @Override
    public void find() {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(USER_API_QUERY_STRING, currentUser);
        String url = getAbsoluteUrlWithQueryString(queryParameters, LISTENER_RELATIVE_URL);

        findRunnable = new LobbyCheckerRunnable(url, currentUser, null);

        // Call L0 lambda function
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("findSpeakers", response.toString());
                        try {
                            if (response.getString("errorMessage").equals("Listener already added. Has a lobby and in queue"))
                                findRunnable.makeListenerDeleteRequest();
                            // Continue only of the response has data.
                            if (!response.get(DATA_JSON_KEY).equals(RESPONSE_NO_DATA))

                                currentActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new Thread(findRunnable).start();
                                    }
                                });
                        } catch (JSONException e) {
                            Log.e("JSONException", "Json response had no '" + DATA_JSON_KEY + "' key");
                            if (findRunnable != null) {
                                findRunnable.setResponseContainer(JSON_FAILED_REQUESTED_OBJECT);
                                findRunnable.setStillChecking(false);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("findSpeakerError", error.toString());
                        if (findRunnable != null) {
                            findRunnable.setResponseContainer(JSON_FAILED_REQUESTED_OBJECT);
                            findRunnable.setStillChecking(false);
                            findRunnable.makeListenerDeleteRequest();
                        }
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");

                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.e("retryVolley", error.getMessage());
            }
        });

        requestQueueSingleton.addToRequestQueue(jsonObjectRequest);
    }

    /**
     * @return the container generated by the findSpeakers() method. It returns null if there is
     * no container.
     */
    @Override
    public JSONObject getContainer() {
        if (findRunnable == null || findRunnable.isStillChecking())
            return JSON_FAILED_REQUESTED_OBJECT;

        return findRunnable.getResponseContainer();
    }

    /**
     * This method removes the speaker from the lobby and adds the listener back to the queue.
     */
    //TODO implement add listener back to queue logic
    @Override
    public void stopFinding() {
        if (findRunnable != null)
            if (findRunnable.isStillChecking())
                findRunnable.setStillChecking(false);
            else
                findRunnable.makeListenerDeleteRequest();
    }

    /**
     * @return the state of the loop from the thread -> if it runs or not
     */
    public boolean isLoopCheckerAlive() {
        return findRunnable != null && findRunnable.isStillChecking();
    }

    /**
     * @return loop count down from the thread to feed a progress bar
     */
    public int getLoopState() {
        if (findRunnable == null)
            return 0;

        return findRunnable.getLoopState();
    }
}
