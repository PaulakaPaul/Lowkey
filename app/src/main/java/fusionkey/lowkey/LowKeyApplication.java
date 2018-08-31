package fusionkey.lowkey;

import android.app.Application;

import fusionkey.lowkey.queue.RequestQueueSingleton;


public class LowKeyApplication extends Application {

    public static RequestQueueSingleton requestQueueSingleton;

    @Override
    public void onCreate() {
        super.onCreate();

        requestQueueSingleton = RequestQueueSingleton.getInstance(this);
    }

}