package com.mapbox.mapboxandroiddemo.examples.basics;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
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

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    this.context = context;
    updateAppWidget(context, appWidgetManager, appWidgetIds[0]);
    enableLocationPlugin();
  }

  private void enableLocationPlugin() {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(context)) {
      // Create a location engine instance
      initializeLocationEngine();
    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(getActivity(context));
    }
  }

  @SuppressWarnings( {"MissingPermission"})
  private void initializeLocationEngine() {
    locationEngine = new LocationEngineProvider(context).obtainBestLocationEngineAvailable();
    locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
    locationEngine.activate();

    Location lastLocation = locationEngine.getLastLocation();

    if (lastLocation != null) {
      getAddress(lastLocation);
    } else {
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

  private void getAddress(Location currentDeviceLocation) {

    // Build a Mapbox reverse geocode request.
    MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
      .accessToken(Mapbox.getAccessToken())
      .query(Point.fromLngLat(currentDeviceLocation.getLongitude(),
        currentDeviceLocation.getLatitude()))
      .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
      .build();

    reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
      @Override
      public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
        List<CarmenFeature> reverseGeocodingResults = response.body().features();
        if (reverseGeocodingResults.size() > 0) {
          String currentLocationAddress = reverseGeocodingResults.get(1).address();
          new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget)
            .setTextViewText(R.id.device_location_textview, currentLocationAddress);
        } else {
          new RemoteViews(context.getPackageName(), R.layout.demo_app_home_screen_widget)
            .setTextViewText(R.id.device_location_textview, context.getString(R.string.widget_unknown_location));
        }
      }

      @Override
      public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
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
    if (locationEngine != null) {
      locationEngine.removeLocationUpdates();
      locationEngine.deactivate();
    }
  }

  @Override
  public void onConnected() {

  }

  @Override
  public void onLocationChanged(Location location) {
    if (location != null) {
      getAddress(location);
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

