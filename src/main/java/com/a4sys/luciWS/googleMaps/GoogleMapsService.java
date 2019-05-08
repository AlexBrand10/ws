package com.a4sys.luciWS.googleMaps;

import com.a4sys.core.dao.ALQueryResult;
import com.a4sys.core.dao.DaoService;
import com.a4sys.luciWS.domain.GeolocalizacionRequest;
import com.a4sys.luciWS.domain.ResultadoGeolocalizacion;
import com.a4sys.luciWS.util.SystemConstants;
import com.a4sys.luciWS.util.Util;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;

/**
 * Created by mmorones on 6/5/17.
 */
public class GoogleMapsService extends DaoService {

    public GoogleMapsService() {
        super(SystemConstants.JDNI);
    }


    /***
     * metodo que obtiene unas coordenadas apartir de una direccion con google maps
     * @param geolocalizacionRequest
     * @return
     */
    public ResultadoGeolocalizacion getPosition(GeolocalizacionRequest geolocalizacionRequest, int tipoDeGeolocalizacion) {

        ResultadoGeolocalizacion resultadoGeolocalizacion = new ResultadoGeolocalizacion();
        GeocodingResult[] results = new GeocodingResult[0];

        try {

            tipoDeGeolocalizacion ++;
            results = getGeocodingResult( tipoDeGeolocalizacion == 1 ?
                    geolocalizacionRequest.getExacta() : tipoDeGeolocalizacion == 2 ?
                    geolocalizacionRequest.getColonia() : tipoDeGeolocalizacion == 3 ? geolocalizacionRequest.getCp() : "" );

            if (results.length > 0) {

                boolean matchEstado = false;
                boolean matchMunicpio = false;
                GeocodingResult result = null;
                for ( int x =  0; x < results.length; x++) {

                    result = results[x];


                    if( tipoDeGeolocalizacion != 3) {
                        //Revisa el Municipio
                        for (int i = 0; i < result.addressComponents.length; i++) {
                            if (result.addressComponents[i].types[0].equals(AddressComponentType.LOCALITY)) {
                                if (Util.eliminarAcentos(result.addressComponents[i].longName)
                                        .contains(geolocalizacionRequest.getMunicipio().toUpperCase())
                                        ||
                                        Util.eliminarAcentos(result.addressComponents[i].shortName)
                                                .contains(geolocalizacionRequest.getMunicipio().toUpperCase())) {
                                    matchMunicpio = true;
                                    break;
                                }
                            }
                        }
                        if (!matchMunicpio && geolocalizacionRequest.getEstado().toUpperCase().equals("CIUDAD DE MEXICO")) {
                            matchMunicpio = true;
                        }
                    }else{
                        matchMunicpio = true;
                    }

                    //Revisa el estado
                    for (int i = 0; i < result.addressComponents.length; i++) {
                        if(result.addressComponents[i].types[0].equals(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1)) {
                            if (Util.eliminarAcentos(result.addressComponents[i].longName)
                                    .contains(geolocalizacionRequest.getEstado().toUpperCase())
                                    ||
                                    Util.eliminarAcentos(result.addressComponents[i].shortName)
                                            .contains(geolocalizacionRequest.getEstado().toUpperCase())
                                    || geolocalizacionRequest.getEstado().toUpperCase()
                                            .contains(Util.eliminarAcentos(result.addressComponents[i].longName))
                                    )
                                     {
                                matchEstado = true;
                                break;
                            }
                        }
                    }



                    if (matchEstado && matchMunicpio) {
                        break;
                    }

                }

                if (matchEstado && matchMunicpio){
                    resultadoGeolocalizacion.setEstatus("0");
                    resultadoGeolocalizacion.setLatitud(String.valueOf(result.geometry.location.lat));
                    resultadoGeolocalizacion.setLongitud(String.valueOf(result.geometry.location.lng));
                    resultadoGeolocalizacion.setTipoGeolocalizacion(String.valueOf(tipoDeGeolocalizacion));

                }else{
                    resultadoGeolocalizacion = getPosition(geolocalizacionRequest,tipoDeGeolocalizacion);
                }

            } else if (results.length == 0 && tipoDeGeolocalizacion <= 3 ){
                resultadoGeolocalizacion =getPosition(geolocalizacionRequest,tipoDeGeolocalizacion);

            } else {
                resultadoGeolocalizacion.setEstatus("-1");
                resultadoGeolocalizacion.setError("No fue posible geolocalizar el Domicilio");

            }


        } catch (Exception e) {
            e.printStackTrace();
            resultadoGeolocalizacion.setEstatus("-1");
            resultadoGeolocalizacion.setError(e.getCause().toString());
        }


        return resultadoGeolocalizacion;
    }

    /***
     * funcion que regresa los resultados de un geocoding
     * @param direccion
     * @return
     */
    private GeocodingResult[] getGeocodingResult(String direccion) {

        GeocodingResult[] results = new GeocodingResult[0];

        direccion = excepciones(direccion);

        if (!"".equals(direccion)) {

            //Obtenemos los datos de key de google
            ALQueryResult data = new ALQueryResult();
            try {
                data = sqlQueryExecutor.executeQueryUsingQueryString("SELECT GOOGLEMAPSKEY FROM TBLCFGSISTEMA");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (data.size() > 0) {

                String key = data.get(0, "GOOGLEMAPSKEY").toString();
                try {
                    GeoApiContext context = new GeoApiContext().setApiKey(key);
                    results = GeocodingApi.geocode(context, direccion).await();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return results;
    }

    /***
     * casos especiales que se encuentra en google maps
     * @param direccion
     * @return
     */
    private String excepciones(String direccion) {

        if(direccion.contains("CIUDAD ADOLFO")){
            direccion = direccion.replace("CIUDAD ADOLFO","");
        }else if(direccion.contains("CIUDAD NEZAHUALCOYOTL")){
            direccion = direccion.replace("CIUDAD","");
        }

        return direccion;
    }

    public File getFileFromGoogleMaps(String latitud, String longitud) {

        File file = null;
        try {

            ALQueryResult alQueryResult =sqlQueryExecutor.executeQueryUsingQueryString("select GOOGLEMAPSKEY from TBLCFGSISTEMA");
            String key=alQueryResult.get(0,"GOOGLEMAPSKEY").toString();
            String ruta="https://maps.googleapis.com/maps/api/staticmap?center="+latitud+","+longitud+"&zoom=16&size=600x400&maptype=roadmap&markers=color:red%7C"+latitud+","+longitud+"&key="+key;
            URL url = new URL(ruta);
            BufferedImage img = ImageIO.read(url);
            file = File.createTempFile("temp",".png");
            ImageIO.write(img, "png", file);
        }catch (Exception e) {
            e.printStackTrace();;
        }

        return file;
    }

}
