package de.dhbw.mosbach.webservices.routing.external.geocoderesponse;

import lombok.Data;

@Data
public class OpenRouteServiceSimplifiedGeocodeResponse {

    private OpenRouteServiceSimplifiedGFeatureResponse[] features;
}
