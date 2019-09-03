package com.EtiennePriou.go4launch.services.places;

import com.EtiennePriou.go4launch.models.PlaceModel;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

public interface PlacesApi {



    void setProximity_radius(Integer proximity_radius);

    void setListPlaces(double latitude, double longitude, GoogleMap map);

    void setNearbyPlaceModelList(List<PlaceModel> nearbyPlaceModelList);

    void setUrlNearbyPlace(double latitude , double longitude);

    void setUrlPlaceDetails(String placeId);



    Integer getProximity_radius();

    List<PlaceModel> getNearbyPlaceModelList();

    PlaceModel getPlaceByReference(String reference);
}
