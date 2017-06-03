package cl.rescuecar.www.rescuecarhelp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class detalleInfo extends ConexionMysqlHelper{

    private static final int DURACION_ANIMACION = 600;
    ImageView perfil;
    ImageView internet;
    int servicioInt, cantidadServ;
    String grut, gdiv, gnombre, gapellido, gtelefono, gemail;
    String rut, time, dist, tip;
    EditText nombre, solicitud, tipo;
    JSONObject jsonObject;
    JSONArray jsonArray;
    TextView rut_user, dig_user, nom_user, ape_user, ema_user, tel_user, patente_serv, eval1, eval2, eval3, eval4, eval5;
    String[] services;
    EditText etNombre, etApellido, etTelefono, etEmail;
    int servp=0,servv=0, serve=0, servh=0;
    ImageView star1, star2, star3, star4, star5, leftArrow;
    String calif, vehiculo, id_mob, evaluaciones, JSON_STRING;
    LinearLayout llinfop, llinfov, llinfoh, llinfoe, lltp, lltv, llte, llth, llMain;
    ImageView imp, imv, ime, imh;
    EditText marca1, modelo1, patente1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_info);

        rut_user = (TextView) findViewById(R.id.tvRut);
        nom_user =(TextView) findViewById(R.id.tvNombre);
        ema_user = (TextView) findViewById(R.id.tvEmail);
        tel_user = (TextView) findViewById(R.id.tvTelefono);
        patente_serv= (TextView) findViewById(R.id.tvPat);


        perfil = (ImageView) findViewById(R.id.improfile);
        internet = (ImageView) findViewById(R.id.imInt);
        leftArrow = (ImageView) findViewById(R.id.imLeftArrow);
        star1 = (ImageView) findViewById(R.id.imStar1);
        star2 = (ImageView) findViewById(R.id.imStar2);
        star3 = (ImageView) findViewById(R.id.imStar3);
        star4 = (ImageView) findViewById(R.id.imStar4);
        star5 = (ImageView) findViewById(R.id.imStar5);
        imp = (ImageView) findViewById(R.id.imp);
        imv = (ImageView) findViewById(R.id.imv);
        ime = (ImageView) findViewById(R.id.ime);
        imh = (ImageView) findViewById(R.id.imh);
        imp.setEnabled(false); imv.setEnabled(false); ime.setEnabled(false); imh.setEnabled(false);
        star5 = (ImageView) findViewById(R.id.imStar5);
        llMain = (LinearLayout) findViewById(R.id.llprincipal);
        lltp = (LinearLayout) findViewById(R.id.lltp);
        lltv = (LinearLayout) findViewById(R.id.lltv);
        llte = (LinearLayout) findViewById(R.id.llte);
        llth = (LinearLayout) findViewById(R.id.llth);
        llinfop = (LinearLayout) findViewById(R.id.llinfoP);
        llinfov = (LinearLayout) findViewById(R.id.llinfoV);
        llinfoe = (LinearLayout) findViewById(R.id.llinfoE);
        llinfoh = (LinearLayout) findViewById(R.id.llinfoH);
        etNombre = (EditText) findViewById(R.id.etName);
        etApellido = (EditText) findViewById(R.id.etApellido);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etTelefono = (EditText) findViewById(R.id.etTelefono);
        eval1 = (TextView) findViewById(R.id.etEval1);
        eval2 = (TextView) findViewById(R.id.etEval2);
        eval3 = (TextView) findViewById(R.id.etEval3);
        eval4 = (TextView) findViewById(R.id.etEval4);
        eval5 = (TextView) findViewById(R.id.etEval5);
        marca1 = (EditText) findViewById(R.id.etMarca1);
        modelo1 = (EditText) findViewById(R.id.etModelo1);
        patente1 = (EditText) findViewById(R.id.etPatente1);


        id_mob = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        escuchaServicios();
        obtenerDatos();

        leftArrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent s = new Intent(getApplicationContext(), MapsActivity.class);
                s.putExtra("rut",rut);
                startActivity(s);
            }
        });

        imp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gnombre = etNombre.getText().toString();
                gapellido = etApellido.getText().toString();
                gemail = etEmail.getText().toString();
                gtelefono = etTelefono.getText().toString();
                etNombre.setText(gnombre) ;
                etApellido.setText(gapellido);
                etTelefono.setText(gtelefono);
                etEmail.setText(gemail);
                nom_user.setText(gnombre+" "+gapellido);
                tel_user.setText("+569"+gtelefono);
                ema_user.setText(gemail);
                ocultarMain(false);
                ocultarInfoPersonal();

                /****** Agrego en base de datos en otro hilo******/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new ConexionMysqlHelper.CargarDatos().execute("http://www.webinfo.cl/soshelp/update_client.php?id_mob="+id_mob+"&rut="+grut+"&div="+gdiv+"&nom="+gnombre+"&ape="+gapellido+"&ema="+gemail+"&tel="+gtelefono);
                                Toast.makeText(getApplicationContext(),"Actualizado correctamente",Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                },0);

            }
        });

        imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: implementar
                ocultarMain(false);
                ocultarMisVehiculos();
                Toast.makeText(getApplicationContext(),"guardar vehiculos",Toast.LENGTH_SHORT).show();
            }
        });

        ime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: implementar
                ocultarMain(false);
                ocultarEvalucaciones();
                Toast.makeText(getApplicationContext(),"guardar evaluaciones",Toast.LENGTH_SHORT).show();
            }
        });

        imh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: implementar
                ocultarMain(false);
                ocultarHistorial();
                Toast.makeText(getApplicationContext(),"guardar historial",Toast.LENGTH_SHORT).show();
            }
        });


        lltp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(servp==0){
                    ocultarMain(true);
                    imp.setImageResource(R.mipmap.save); imp.setEnabled(true);
                    Util.expand(llinfop,DURACION_ANIMACION);
                    servp=1;
                    etNombre.setText(gnombre) ;
                    etApellido.setText(gapellido);
                    etTelefono.setText(gtelefono);
                    etEmail.setText(gemail);
                    ocultarMisVehiculos();
                    ocultarEvalucaciones();
                    ocultarHistorial();
                }else{
                    ocultarMain(false);
                    ocultarInfoPersonal();
                }

            }
        });

        lltv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(servv==0){
                    ocultarMain(true);
                    imv.setImageResource(R.mipmap.save); imv.setEnabled(true);
                    Util.expand(llinfov, DURACION_ANIMACION);
                    servv=1;
                    ocultarInfoPersonal();
                    ocultarEvalucaciones();
                    ocultarHistorial();
                }else{
                    ocultarMain(false);
                    ocultarMisVehiculos();
                }
            }
        });

        llte.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(serve==0){
                    ocultarMain(true);
                    ime.setImageResource(R.mipmap.save);ime.setEnabled(true);
                    Util.expand(llinfoe, DURACION_ANIMACION);
                    serve=1;
                    ocultarInfoPersonal();
                    ocultarMisVehiculos();
                    ocultarHistorial();
                }else{
                    ocultarMain(false);
                    ocultarEvalucaciones();
                }

            }
        });

        llth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(servh==0){
                    ocultarMain(true);
                    imh.setImageResource(R.mipmap.save); imh.setEnabled(true);
                    Util.expand(llinfoh, DURACION_ANIMACION);
                    servh=1;
                    ocultarInfoPersonal();
                    ocultarMisVehiculos();
                    ocultarEvalucaciones();
                }else{
                    ocultarMain(false);
                    ocultarHistorial();
                }

            }
        });

    }

    private void ocultarInfoPersonal() {
        Util.collapse(llinfop, DURACION_ANIMACION);
        servp=0;
        imp.setImageResource(R.mipmap.abajosi);
        imp.setEnabled(false);
    }

    private void ocultarMisVehiculos() {
        imv.setImageResource(R.mipmap.abajosi);
        imv.setEnabled(false);
        Util.collapse(llinfov, DURACION_ANIMACION);
        servv=0;
    }

    private void ocultarEvalucaciones() {
        ime.setImageResource(R.mipmap.abajosi);
        ime.setEnabled(false);
        Util.collapse(llinfoe, DURACION_ANIMACION);
        serve=0;
    }

    private void ocultarHistorial() {
        imh.setImageResource(R.mipmap.abajosi);
        imh.setEnabled(false);
        Util.collapse(llinfoh, DURACION_ANIMACION);
        servh=0;
    }

    public void ocultarMain(boolean ocultar) {
        if (ocultar){
            Util.collapse(llMain, DURACION_ANIMACION);
        }else{
            Util.expand(llMain, DURACION_ANIMACION);
        }
    }

    public void escuchaServicios() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            internet.setImageResource(R.drawable.int_si);
            servicioInt = 1;
        } else {
            internet.setImageResource(R.drawable.int_no);
            Toast.makeText(getApplicationContext(), "¡¡ Tu teléfono no esta conectado a internet!!", Toast.LENGTH_SHORT).show();
            servicioInt = 0;
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
        }, 50000);

    }

    private void obtenerDatos() {

        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            grut = (String) b.get("rut");
            gdiv = (String) b.get("div");
            gnombre = (String) b.get("nom");
            gapellido = (String) b.get("ape");
            gtelefono = (String) b.get("tel");
            gemail = (String) b.get("ema");
        }
        Toast.makeText(this, "Telefono"+gtelefono, Toast.LENGTH_SHORT).show();

        rut_user.setText(grut+"-"+gdiv) ;
        nom_user.setText(gnombre+" "+gapellido);
        ema_user.setText(gemail);
        tel_user.setText("+569"+gtelefono);

        BuscarDetalles();
    }

    public void BuscarDetalles() {

        new BackgroundTask().execute();

    }

    class BackgroundTask extends AsyncTask<Void, Void, String> {
        String json_url;

        @Override
        protected void onPreExecute() {

            json_url = "http://www.webinfo.cl/soshelp/cons_chofer_client.php?rut="+grut;

        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while ((JSON_STRING = bufferedReader.readLine()) != null) {
                    stringBuilder.append(JSON_STRING + "\n");
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
            JSON_STRING = null;
            JSON_STRING = result;
            presentarDatos();

        }
    }

    public void presentarDatos() {
        if (JSON_STRING != null) {

            try {
                jsonObject = new JSONObject(JSON_STRING);
                jsonArray = jsonObject.getJSONArray("server_response");

                JSONObject JO = jsonArray.getJSONObject(0);
                calif = JO.getString("calificaciones");
                vehiculo = JO.getString("vehiculo");
                evaluaciones = JO.getString("evaluaciones");

                if (evaluaciones!=null){
                    //String[]  infoE = evaluaciones.split("&");

                   eval1.setText(evaluaciones);


                }

                String[]  infoC = calif.split(",");

                if (infoC.length >= 1) {

                    int valoracion = Integer.parseInt(infoC[0]);

                    Toast.makeText(getApplicationContext(),"Valor : "+valoracion,Toast.LENGTH_SHORT).show();

                    switch (valoracion){

                        case 1:
                            star1.setImageResource(R.drawable.starup);
                            break;
                        case 2:
                            star1.setImageResource(R.drawable.starup);
                            star2.setImageResource(R.drawable.starup);
                            break;
                        case 3:
                            star1.setImageResource(R.drawable.starup);
                            star2.setImageResource(R.drawable.starup);
                            star3.setImageResource(R.drawable.starup);
                            break;
                        case 4:
                            star1.setImageResource(R.drawable.starup);
                            star2.setImageResource(R.drawable.starup);
                            star3.setImageResource(R.drawable.starup);
                            star4.setImageResource(R.drawable.starup);
                            break;
                        case 5:
                            star1.setImageResource(R.drawable.starup);
                            star2.setImageResource(R.drawable.starup);
                            star3.setImageResource(R.drawable.starup);
                            star4.setImageResource(R.drawable.starup);
                            star5.setImageResource(R.drawable.starup);
                            break;
                    }

                }else{

                    Toast.makeText(getApplicationContext(),"No se ha encontrado evaluación",Toast.LENGTH_SHORT).show();

                }

                String[]  infoV = vehiculo.split(",");

                if (infoV[0]!=null){

                    patente_serv.setText(infoV[0]+" "+infoV[1]+" PAT: "+infoV[2]);
                    marca1.setText(infoV[0]);
                    modelo1.setText(infoV[1]);
                    patente1.setText(infoV[2]);

                }else {

                    patente_serv.setText("Vehículo no encontrado");


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
