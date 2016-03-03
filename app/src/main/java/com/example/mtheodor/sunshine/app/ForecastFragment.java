package com.example.mtheodor.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    protected ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
        forecastAdapter = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_refresh) {
            refreshAction();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        final List<String> weekForecast = getSampleForecast();
        forecastAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast
        );

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = weekForecast.get(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();

                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    private List<String> getSampleForecast() {
        String[] data = {
                "Mon 6/23 - Sunny - 31/17"
        };
        return formatToAdapter(data);
    }

    private List<String> formatToAdapter(String[] data) {
        return new ArrayList<>(Arrays.asList(data));
    }

    private void refreshAction() {
        new FetchWeatherTask().execute(getPostalCode());
    }

    private String getPostalCode() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences.getString(getString(SettingsActivity.LOCATION), "");
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            return new WeatherForecastRepository(params[0]).fetch();
        }

        @Override
        protected void onPostExecute(String[] forecast) {
            if (forecast == null) {
                return;
            }

            forecastAdapter.clear();
            List<String> weeklyForecast = formatToAdapter(forecast);
            for (String dailyForecast: weeklyForecast) {
                forecastAdapter.add(dailyForecast);
            }
        }
    }
}
