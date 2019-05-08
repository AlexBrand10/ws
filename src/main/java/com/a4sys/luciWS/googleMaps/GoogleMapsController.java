package com.a4sys.luciWS.googleMaps;

import com.a4sys.luciWS.domain.GeolocalizacionRequest;
import com.a4sys.luciWS.domain.ResultadoGeolocalizacion;
import com.a4sys.luciWS.util.Util;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Created by mmorones on 6/5/17.
 */
@Path("/googleMaps")
public class GoogleMapsController {


    /***
     * metodo que obtiene unas coordenadas apartir de una direccion con google maps
     * @param geolocalizacionRequest
     * @return
     */
    @POST
    @Path("/getPosition")
    public Response respuestaSMSDosVias( GeolocalizacionRequest geolocalizacionRequest ) {

        ResultadoGeolocalizacion resultadoGeolocalizacion =  new ResultadoGeolocalizacion();
        Response.ResponseBuilder response = null;

        GoogleMapsService service = new GoogleMapsService();
        try {
            resultadoGeolocalizacion = service.getPosition(geolocalizacionRequest, new Integer(geolocalizacionRequest.getNivelPrecision()));
        } catch (Exception e) {

        } finally {
            service.closeService();
            System.gc();
        }

        response = Response.ok(resultadoGeolocalizacion).type(MediaType.APPLICATION_JSON+ ";charset=utf-8");
        return response.build();
    }


    @GET
    @Path("/getMap")
    public Response getFileFromS3Amazon(@QueryParam("latitud") String latitud, @QueryParam("longitud") String longitud) {

        Response.ResponseBuilder response = null;
        GoogleMapsService service = new GoogleMapsService();

        try {
            File file  = service.getFileFromGoogleMaps(latitud,longitud);
            response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachment; filename="+file.getName());
        } catch (Exception e) {
            response = Response.ok(Util.setResponseJson("-1", e.getCause() == null ? e.getMessage() : e.getCause().toString(), null), MediaType.APPLICATION_JSON + ";charset=utf-8");
        } finally {
            service.closeService();
        }

        return response.build();

    }

}


