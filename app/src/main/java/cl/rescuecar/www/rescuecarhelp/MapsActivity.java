package cl.rescuecar.www.rescuecarhelp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends ConexionMysqlHelper implements OnMapReadyCallback {

    private GoogleMap mMap;
    Switch serv;
    TextView est, dir, lt, lg,con, labelConductor;
    LocationManager locationManager;
    Location location;
    double lat = 0.0;
    double lng = 0.0;
    Marker marcador;
    AlertDialog alert = null;
    AlertDialog alert2 = null;
    String ciudad, ciudad2,direc, id_mob, json_string, JSON_STRING;
    int estCon=0, cuentaAlerta=0, servicioGPS, servicioInt;
    JSONObject jsonObject;
    JSONArray jsonArray;
    MediaPlayer eco;
    ImageView gps,internet, setting;
    String rut, div, nombre, apellido, nomCompleto, tipo, telefono;
    String gnombre, gapellido, grut, gdiv, gtelefono, gservicios, gemail, gvehiculo;
    List<String> servicios = new ArrayList<String>();
    String[] services;
    int contadorServ, cantidadServ=0, tomadoServicio=0;
    int enServicio;
    String id_alert, tip_alert, rut_cond, text_alert;
    double lati=0.0, lngi=0.0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        id_mob = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        //Creacion de boton y estado

        eco = MediaPlayer.create(this,R.raw.eco2);
        serv = (Switch) findViewById(R.id.swServ);
        gps = (ImageView) findViewById(R.id.img_gps);
        internet = (ImageView) findViewById(R.id.img_int);
        setting = (ImageView) findViewById(R.id.imSetting);
        labelConductor = (TextView) findViewById(R.id.tvConductor);


        enServicio=0;

        setting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent s = new Intent(getApplicationContext(), detalleInfo.class);
                s.putExtra("rut",grut);
                s.putExtra("div",gdiv);
                s.putExtra("nom",gnombre);
                s.putExtra("ape",gapellido);
                s.putExtra("tel",gtelefono);
                s.putExtra("ema",gemail);
                startActivity(s);
            }
        });

        serv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    estCon=1;
                    Toast.makeText(getApplicationContext(),"Activando Servicios",Toast.LENGTH_SHORT).show();
                    BuscarAlerta();
                    activarChofer();

                } else {

                    estCon=0;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /******Envio de datos a mysql ******/
                                    new CargarDatos().execute("http://www.webinfo.cl/soshelp/del_driv.php?id_mob="+id_mob);
                                }
                            });
                        }
                    },0);
                }
            }
        });

        //Escucha servicios
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        escuchaServicios();
        //fin escucha servicios

        obtenerDatos();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        miUbicacion();
        // Controles UI
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar diálogo explicativo
            } else {
                // Solicitar permiso
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
            }
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    private void obtenerDatos() {
        final varGlob  varglob = (varGlob) getApplicationContext();

        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            grut = (String) b.get("rut");
            gdiv = (String) b.get("div");
            gnombre = (String) b.get("nom");
            gapellido = (String) b.get("ape");
            gtelefono = (String) b.get("tel");
            gemail = (String) b.get("ema");
            gservicios = (String) b.get("serv");
            gvehiculo = (String) b.get("veh");
            labelConductor.setText("Su nombre : " + gnombre + " " + gapellido);

        }

        if (gservicios!=null || gservicios!=""){

            services = gservicios.split(",");
            cantidadServ = services.length;


        }else{
            Toast.makeText(getApplicationContext(), "No tiene servicios registrados", Toast.LENGTH_SHORT).show();

        }

    }

    private void agregarMarcador(double lat, double lng) {
        LatLng coordenadas = new LatLng(lat, lng);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 15);
        if (marcador != null) marcador.remove();
        marcador = mMap.addMarker(new MarkerOptions()
                .position(coordenadas)
                .visible(false)
                .title("Mi Ubicación"));
        mMap.animateCamera(miUbicacion);
    }


    private void actualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            agregarMarcador(lat, lng);
            if (lat!=0.0 && lng!=0.0){
                try{
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> list = geocoder.getFromLocation(lat,lng,5);
                    if (!list.isEmpty()){
                        Address direccion = list.get(0);
                        ciudad = direccion.getLocality();
                        ciudad2 = direccion.getSubAdminArea();
                        direc = direccion.getAddressLine(0);
                        ciudad=ciudad.replaceAll(" ", "%20");
                       activarChofer();
                    }
                }catch (IOException e){
                    //dir.setText(""+e);

                }
            }

        }
    }

    LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            actualizarUbicacion(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    private void miUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        actualizarUbicacion(location);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500,0,locListener);
    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert2 = builder.create();
        alert2.show();
    }


    @Override
    protected void onResume() {
        if (enServicio==1){
            serv.setChecked(true);

        }
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500, 0, locListener);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locListener);
    }

    private void activarChofer() {

        if (estCon==1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /******Envio de datos a mysql ******/
                            Log.i("Act loc","Chofer");
                            new CargarDatos().execute("http://www.webinfo.cl/soshelp/act_driv.php?rut_driv="+grut+"&lat="+lat+"&lng="+lng);
                            secuencia();

                        }
                    });
                }
            },0);
        }
    }

    public void BuscarAlerta() {

        if (estCon==1 && enServicio == 0 ) {
            Log.i("Buscando","Alerta");
            new BackgroundTask().execute();
        }
    }
    class BackgroundTask extends AsyncTask<Void,Void,String>
    {
        String json_url;
        @Override
        protected void onPreExecute() {

            //Toast.makeText(MapsActivity.this, "Buscando : "+services[contadorServ], Toast.LENGTH_SHORT).show();
            Log.i("Buscando ",""+services[contadorServ]);
            json_url = "http://www.webinfo.cl/soshelp/cons_alerta.php?tipo="+services[contadorServ];
            contadorServ++;
            if (contadorServ>=cantidadServ){
                contadorServ=0;
            }

        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while ((JSON_STRING = bufferedReader.readLine())!=null)
                {
                    stringBuilder.append(JSON_STRING+"\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            JSON_STRING=null;
            JSON_STRING = result;
            presentarDatos();

        }
    }
    public void presentarDatos() {
        if (JSON_STRING != null) {

                try {
                    jsonObject = new JSONObject(JSON_STRING);
                    jsonArray = jsonObject.getJSONArray("server_response");
                    int count=0;


                        JSONObject JO = jsonArray.getJSONObject(count);
                        id_alert = JO.getString("id_alert");
                        rut_cond = JO.getString("rut_cond");
                        text_alert = JO.getString("text_alert");
                        tip_alert = JO.getString("tip_alert");
                        lati = Double.parseDouble(JO.getString("lat_cond"));
                        lngi = Double.parseDouble(JO.getString("lng_cond"));



                    if (rut_cond.length()>2){
                        Toast.makeText(this, "Rut conductor informacion de conductor : "+rut_cond, Toast.LENGTH_SHORT).show();

                        confirmaAlerta();
                            //AlertAlerta(id_alert, tip_alert, text_alert,rut_cond,lati, longi);
                        }else{

                            BuscarAlerta();

                        }

                    cuentaAlerta++;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    private void confirmaAlerta(){
        eco.start();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<font color='#6E6E6E'>Nueva solicitud de servicios</font>"));
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("<font color='#585858'>Nueva solicitud de servicio para : </font>");
        mensaje.append("<font color='#FA5858'>").append(tipoAlerta(tip_alert)).append("</font> ");
        mensaje.append("<font color='#585858'> con la siguiente observación : </font>");
        mensaje.append("<font color='#FA5858'>").append(text_alert).append("</font> ");
        builder.setMessage(Html.fromHtml(mensaje.toString()))
                .setCancelable(false)
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                eco.stop();
                                alert.cancel();
                                BuscarAlerta();
                            }
                        }, 100000);

                    }
                })
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        eco.stop();
                        alert.cancel();
                        enServicio=1;


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new CargarDatos().execute("http://www.webinfo.cl/soshelp/del_driv.php?id_mob="+id_mob);
                                    }
                                });
                            }
                        },0);

                        Intent m = new Intent(getApplicationContext(), Maps2Activity.class);
                        m.putExtra("id_alert", id_alert);
                        m.putExtra("lat", lat);
                        m.putExtra("lng", lng);
                        m.putExtra("text_alert", text_alert);
                        m.putExtra("rut", grut);
                        m.putExtra("tip_driv", tip_alert);
                        m.putExtra("lati", lati);
                        m.putExtra("lngi", lngi);
                        startActivity(m);

                    }
                });

                if (enServicio==0) {
                    alert = builder.create();
                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Window view = ((AlertDialog)dialog).getWindow();
                            view.setBackgroundDrawableResource(R.drawable.alert_dialog_background);
                        }
                    });
                    alert.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            eco.pause();
                            alert.hide();
                            alert.dismiss();
                            alert.cancel();
                        }
                    }, 11000);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BuscarAlerta();
                        }
                    }, 20000);
                }
    }


    public String tipoAlerta(String tip_alerta){
        String texto = null;

        switch (tip_alerta){
            case "gm":
                texto = "solicitud de grúa para motocicleta";
                break;
            case "ga":
                texto = "solicitud de grúa para vehículo";
                break;
            case "gc":
                texto = "solicitud de grúa para camioneta";
                break;
            case "go":
                texto = "solicitud de grúa para vehículo mayor";
                break;
            case "po":
                texto = "solicitud de carabineros";
                break;
            case "am":
                texto = "solicitud de ambulancia";
                break;
            case "bo":
                texto = "solicitud de bomberos";
                break;
            case "me":
                texto = "solicitud de mecánico en ruta";
                break;
            case "ne":
                texto = "solicitud de asistencia de neumático";
                break;
            case "tr":
                texto = "solicitud de servicio de transporte";
                break;
            case "co":
                texto = "solicitud de servicio de combustible";
                break;

        }

    return texto;
    }

    public void escuchaServicios(){

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gps.setImageResource(R.drawable.gps_no);
            servicioGPS=0;
            AlertNoGps();
        }else{
            gps.setImageResource(R.drawable.gps_si);
            servicioGPS=1;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null){
            internet.setImageResource(R.drawable.int_si);
            servicioInt=1;
        }else{
            internet.setImageResource(R.drawable.int_no);
            Toast.makeText(getApplicationContext(),"¡¡ Tu teléfono no esta conectado a internet!!",Toast.LENGTH_SHORT).show();
            servicioInt=0;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        escuchaServicios();
                    }
                });
            }
        },10000);

    }

    private void secuencia() {

        if (estCon == 1) {

            gps.setImageResource(R.drawable.gps_si_conect);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gps.setImageResource(R.drawable.gps_si);
                            secuencia();
                        }
                    });
                }
            }, 1000);
        }
    }
}