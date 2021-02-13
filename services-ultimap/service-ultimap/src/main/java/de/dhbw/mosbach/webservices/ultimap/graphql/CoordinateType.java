package de.dhbw.mosbach.webservices.ultimap.graphql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateType {
    private double lat;
    private double lon;
}