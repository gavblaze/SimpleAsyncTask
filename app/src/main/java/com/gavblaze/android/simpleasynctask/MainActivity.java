package com.gavblaze.android.simpleasynctask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private TextView mPercentCompleteTextView;
    private ProgressBar mProgressBar;
    private static final String KEY = "key";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.textView1);
        mProgressBar = findViewById(R.id.progressBar);
        mPercentCompleteTextView = findViewById(R.id.percentTextView);

        if (savedInstanceState != null) {
            mTextView.setText(savedInstanceState.getString(KEY));
        }
    }

    public void startTask(View view) {
        mProgressBar.clearAnimation();
        mProgressBar.setVisibility(View.VISIBLE);
        // Put a message in the text view
        mTextView.setText(R.string.workin_on_it);
        SimpleAsyncTask task = new SimpleAsyncTask(mTextView, mProgressBar, mPercentCompleteTextView);
        task.execute();
    }


    private static class SimpleAsyncTask extends AsyncTask<Void, Integer, String> {
        /*What is the weak reference (the WeakReference class) for?
        If you pass a TextView into the AsyncTask constructor and then store it in a member variable,
        that reference to the TextView means the Activity cannot ever be garbage collected and thus leaks memory,
        even if the Activity is destroyed and recreated as in a device configuration change.
        This is called creating a leaky context, and Android Studio will warn you if you try it.
         The weak reference prevents the memory leaks by allowing the object held by that reference to be garbage collected if necessary.*/
        private WeakReference<TextView> mTextView;
        private WeakReference<ProgressBar> mProgressBar;
        private WeakReference<TextView> mTextViewPcComplete;


        SimpleAsyncTask(TextView tv, ProgressBar pb ,TextView pc) {
            mTextView = new WeakReference<>(tv);
            mProgressBar = new WeakReference<>(pb);
            mTextViewPcComplete = new WeakReference<>(pc);
        }


        @Override
        protected String doInBackground(Void... voids) {
            Log.i(LOG_TAG, "TEST............doInBackground() called");
            Random r = new Random();
            int n = r.nextInt(11);
            int s = n * 800;

            for (int percentProgress = 0; percentProgress <= 100; percentProgress += 5) {

                try {
                    // with an increment of 5 we will have 20 chunks so we need to break up the sleep time into 20 chunks
                    Thread.sleep(s / 20);
                    publishProgress(percentProgress);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //finally return the total time
            return "Awake at last after sleeping for " + s + " milliseconds!";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i(LOG_TAG, "TEST............onProgressUpdate() called");
            Log.i(LOG_TAG, "PROGRESS..................." + values[0]);
            mProgressBar.get().setProgress(values[0]);
            String message = "Percentage complete: " + values[0] + "%";
            mTextViewPcComplete.get().setText(message);
        }


        @Override
        protected void onPostExecute(String s) {
            Log.i(LOG_TAG, "TEST............onPostExecute() called");
            super.onPostExecute(s);
            /*Because mTextView is a weak reference,
            you have to reference it with the get() method to get the underlying TextView object,
            and to call setText() on it.*/
            mTextView.get().setText(s);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY, mTextView.getText().toString());
    }
}

/*1. When you rotate the device, the system restarts the app, calling onDestroy() and then onCreate().
The AsyncTask will continue running even if the activity is destroyed, but it will lose the ability to report back to the activity's UI.
It will never be able to update the TextView that was passed to it, because that particular TextView has also been destroyed.*/

/*2. Once the activity is destroyed the AsyncTask will continue running to completion in the background, consuming system resources.
Eventually, the system will run out of resources, and the AsyncTask will fail.*/


/*For these reasons, an AsyncTask is not well suited to tasks which may be interrupted by the destruction of the Activity.
In use cases where this is critical you can use a different type of background class called an AsyncTaskLoader which you will learn about in a later practical.

In order to prevent the TextView from resetting to the initial string, you need to save its state.
You will now implement onSaveInstanceState() to preserve the content of your TextView
when the activity is destroyed in response to a configuration change such as device rotation.*/