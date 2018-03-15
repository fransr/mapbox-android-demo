package com.mapbox.mapboxandroiddemo.utils;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.location.LocationEngineProvider;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Implementation of App Widget functionality.
 */
public class DemoAppHomeScreenWidget extends AppWidgetProvider implements LocationEngineListener, PermissionsListener {

  private PermissionsManager permissionsManager;
  private LocationEngine locationEngine;
  private Context context;
  private String TAG = "DemoAppHomeScreenWidget";
  private int singleWidgetId;
  private Location lastLocation;

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    Log.d(TAG, "onUpdate: ");

    this.singleWidgetId = appWidgetIds[0];
    this.context = context;
//    enableLocationPlugin();

   /* // Construct the RemoteViews object
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetIds[0], views);*/
  }

  @Override
  public void onEnabled(Context context) {
    Log.d(TAG, "onEnabled: ");
    this.context = context;
    enableLocationPlugin();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      initializeLocationEngine();
    } else {
      Toast.makeText(context, R.string.user_location_permission_not_granted, Toast.LENGTH_SHORT).show();
    }
  }

  private void enableLocationPlugin() {
    Log.d(TAG, "enableLocationPlugin: starting");
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(context)) {
      Log.d(TAG, "enableLocationPlugin: permissions already granted");
      // Create a location engine instance
      initializeLocationEngine();
    } else {
      Log.d(TAG, "enableLocationPlugin: permissions not granted yet");
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(getActivity(context));
    }
  }

  @SuppressWarnings( {"MissingPermission"})
  private void initializeLocationEngine() {
    Log.d(TAG, "initializeLocationEngine: starting");
    locationEngine = new LocationEngineProvider(context).obtainBestLocationEngineAvailable();
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.activate();
    Log.d(TAG, "initializeLocationEngine: locationEngine.activate();");

    lastLocation = locationEngine.getLastLocation();

    getReverseGeocodeData(lastLocation);
  }

  public Activity getActivity(Context context) {
    if (context == null) {
      return null;
    } else if (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        return (Activity) context;
      } else {
        return getActivity(((ContextWrapper) context).getBaseContext());
      }
    }
    return null;
  }

  private void getReverseGeocodeData(Location currentDeviceLocation) {

    Log.d(TAG, "getReverseGeocodeData: starting");

    Log.d(TAG, "getReverseGeocodeData: currentDeviceLocation.getLatitude() = " + currentDeviceLocation.getLatitude());
    Log.d(TAG, "getReverseGeocodeData: currentDeviceLocation.getLongitude() = " + currentDeviceLocation.getLongitude());

    // Build a Mapbox reverse geocode request.
    MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
      .accessToken(context.getString(R.string.access_token))
      .query(Point.fromLngLat(-122.40001555, 37.78774203))
      .build();

    reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
      @Override
      public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
        Log.d(TAG, "onResponse: response.body() = " + response.body());


        if (response.body().features().size() > 0 && !response.body().features().get(0).placeName().isEmpty()) {

          Log.d(TAG, "onResponse: response.body().features().get(0).placeName() = " +
            response.body().features().get(0).placeName());
/*

          RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);
          remoteViews.setTextViewText(R.id.device_location_textview,
            response.body().features().get(0).placeName());
          AppWidgetManager.getInstance(context).updateAppWidget(singleWidgetId, remoteViews);
*/

          AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

          RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);
          appWidgetManager.updateAppWidget(new ComponentName(context, DemoAppHomeScreenWidget.class), views);


        } else {
          RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);
          new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget)
            .setTextViewText(R.id.device_location_textview, context.getString(R.string.widget_unknown_location));
          AppWidgetManager.getInstance(context).updateAppWidget(singleWidgetId, remoteViews);

        }
      }

      @Override
      public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
        Log.d(TAG, "onFailure: geocoding failure");
        throwable.printStackTrace();
      }
    });
  }

  @Override
  public void onDisabled(Context context) {
    // TODO: Enter relevant functionality for when the last widget is disabled
  }

  @Override
  public void onConnected() {

  }

  @Override
  public void onLocationChanged(Location location) {
    if (location != null) {
      Log.d(TAG, "initializeLocationEngine: lastLocation != null");
//      getReverseGeocodeData(location);
    } else {
      Log.d(TAG, "initializeLocationEngine: lastLocation == null");
//      locationEngine.addLocationEngineListener(this);
    }
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {

  }
}

