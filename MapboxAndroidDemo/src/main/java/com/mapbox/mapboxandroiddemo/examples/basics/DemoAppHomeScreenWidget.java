package com.mapbox.mapboxandroiddemo.examples.basics;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
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

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    this.context = context;
    this.singleWidgetId = appWidgetIds[0];
    updateAppWidget(context, appWidgetManager, appWidgetIds[0]);
    enableLocationPlugin();
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

    Location lastLocation = locationEngine.getLastLocation();

    if (lastLocation != null) {
      Log.d(TAG, "initializeLocationEngine: lastLocation != null");
//      getAddress(lastLocation);
    } else {
      Log.d(TAG, "initializeLocationEngine: lastLocation == null");
      getAddress(null, 38.899463, -77.033308);

      locationEngine.addLocationEngineListener(this);
    }
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

  static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId) {

    // Construct the RemoteViews object
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views);
  }

  private void getAddress(@Nullable Location currentDeviceLocation, @Nullable double longitude,
                          @Nullable double latitude) {

    Log.d(TAG, "getAddress: starting");

    // Build a Mapbox reverse geocode request.
    MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
      .accessToken(context.getString(R.string.access_token))
      .query(Point.fromLngLat(latitude, longitude))
      .geocodingTypes(GeocodingCriteria.TYPE_COUNTRY)
      .build();

    reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
      @Override
      public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

        Log.d(TAG, "onResponse: ");

        String placeName = response.body().features()
          .get(0).placeName();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget);
        remoteViews.setTextViewText(R.id.device_location_textview, placeName);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        manager.updateAppWidget(singleWidgetId, remoteViews);

//        new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget)
//          .setTextViewText(R.id.device_location_textview, context.getString(R.string.widget_unknown_location));
      }

      @Override
      public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
        Log.d(TAG, "onFailure: geocoding failure");
        throwable.printStackTrace();
      }
    });
  }

  @Override
  public void onEnabled(Context context) {
    // TODO: Enter relevant functionality for when the first widget is created

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
//      getAddress(location);
    }
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {

  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      enableLocationPlugin();
    } else {
      Toast.makeText(context, R.string.user_location_permission_not_granted, Toast.LENGTH_SHORT).show();
    }
  }
}

