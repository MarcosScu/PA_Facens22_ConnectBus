package com.facens.conectbus.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.facens.conectbus.config.ConfiguracaoFirebase;
import com.facens.conectbus.helper.UsuarioFirebase;
import com.facens.conectbus.model.Destino;
import com.facens.conectbus.model.Requisicao;
import com.facens.conectbus.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.BoringLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facens.conectbus.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth auth;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private EditText editDestino;
    private LatLng minhaLoc;

    private LinearLayout linearLayoutDestino;
    private Button buttonSolicitarRastreio;
    private Boolean cancelarRastreio = false;
    private DatabaseReference databaseReference;
    private Requisicao requisicao;

    private Usuario passageiro;
    private Usuario motorista;
    private LatLng localMotorista;

    private String statusRequisicao;
    private Destino destino;
    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);


        initComponents();
        
        verificaStatusRequisicao();


    }

    private void verificaStatusRequisicao() {

        Usuario usuario = UsuarioFirebase.getDadoUsuarioLogado();
        DatabaseReference requisicoes = databaseReference.child("requisicoes");
        Query requicicaoPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo(usuario.getId());


        requicicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                List<Requisicao> list = new ArrayList<>();
                for (DataSnapshot ds: snapshot.getChildren()){
                    list.add(ds.getValue(Requisicao.class));
                }

                Collections.reverse( list );
                if (list != null && list.size() > 0){
                    requisicao = list.get(0);

                    if(requisicao!=null){
                        if( !requisicao.getStatus().equals(Requisicao.STATUS_ENCERRADA)) {
                            passageiro = requisicao.getPassageiro();
                            minhaLoc = new LatLng(
                                    Double.parseDouble(passageiro.getLatitude()),
                                    Double.parseDouble(passageiro.getLongitude())
                            );

                            statusRequisicao = requisicao.getStatus();
                            destino = requisicao.getDestino();
                            if (requisicao.getMotorista() != null) {
                                motorista = requisicao.getMotorista();
                                localMotorista = new LatLng(
                                        Double.parseDouble(motorista.getLatitude()),
                                        Double.parseDouble(motorista.getLongitude())
                                );
                            }
                            alteraInterfaceStatusRequisicao(statusRequisicao);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void alteraInterfaceStatusRequisicao(String status) {

        if (status != null && !status.isEmpty()){
            cancelarRastreio = false;
            switch (status) {
                case Requisicao.STATUS_AGUARDANDO:
                    requisicaoAguardando();
                    break;

                case Requisicao.STATUS_A_CAMINHO:
                    requisicaoaCaminho();
                    break;

                case Requisicao.STATUS_VIAGEM:
                    requisicaoViagem();
                    break;

                case Requisicao.STATUS_FINALIZADA:
                    requisicaoFinalizada();
                    break;
                case Requisicao.STATUS_CANCELADA:
                    requisicaoCancelada();
                    break;

            }
    }else {
            adicionarMarcadorPassageiro(minhaLoc, "Seu Local");
            centralizarMarcador(minhaLoc);
        }
    }

    private void requisicaoCancelada() {

        linearLayoutDestino.setVisibility(View.VISIBLE);
        buttonSolicitarRastreio.setText("SOLICITAR RASTREIO DA MELHOR LINHA");
        cancelarRastreio = false;

    }


    private void requisicaoAguardando(){

        linearLayoutDestino.setVisibility(View.GONE);
        buttonSolicitarRastreio.setText("AGUARDANDO CONFIRMAÇÃO DE RASTREIO");
        cancelarRastreio = true;

        adicionarMarcadorPassageiro(minhaLoc, passageiro.getNome());
        centralizarMarcador(minhaLoc);


    }

    private void requisicaoaCaminho(){

        linearLayoutDestino.setVisibility(View.GONE);
        buttonSolicitarRastreio.setText("RASTREAMENTO EM ANDAMENTO");
        cancelarRastreio = true;

        adicionarMarcadorPassageiro(minhaLoc, passageiro.getNome());
        adicionarMarcadorMotorista(localMotorista, motorista.getNome());
        centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);


    }

    private void requisicaoViagem(){
        cancelarRastreio = true;

        linearLayoutDestino.setVisibility(View.GONE);
        buttonSolicitarRastreio.setText("RASTREAMENTO EM ANDAMENTO");
        adicionarMarcadorMotorista(localMotorista, motorista.getNome());
        LatLng localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionarMarcadorDestino(localDestino,"Destino");
        centralizarDoisMarcadores(marcadorMotorista, marcadorDestino);
    }

    private void requisicaoFinalizada(){
        cancelarRastreio = true;

        linearLayoutDestino.setVisibility(View.GONE);
        buttonSolicitarRastreio.setText("ÁREA DE DESTINO ATINGIDA");

        LatLng localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionarMarcadorDestino(localDestino,"Destino");
        centralizarMarcador(localDestino);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Destino atingido")
                .setMessage("Você chegou em seu destino aproximado.\nObrigado por contar conosco!")
                .setCancelable(false)
                .setNegativeButton("Encerrar rastreamento do ônibus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
                        requisicao.atualizarStatus();

                        finish();
                        startActivity(new Intent(getIntent()));
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void adicionarMarcadorDestino(LatLng localizacao, String titulo){

        if( marcadorPassageiro != null)
            marcadorPassageiro.remove();

        if( marcadorDestino != null)
            marcadorDestino.remove();

        marcadorDestino = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino_icon  ))
        );
    }

    private void adicionarMarcadorPassageiro(LatLng localizacao, String titulo){

        if( marcadorPassageiro != null)
            marcadorPassageiro.remove();

        marcadorPassageiro = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario_icon  ))
        );
    }

    private void adicionarMarcadorMotorista(LatLng localizacao, String titulo){

        if( marcadorMotorista != null)
            marcadorMotorista.remove();

        marcadorMotorista = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon  ))
        );
    }

    private void centralizarMarcador(LatLng local){
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(local, 20)
        );
    }

    private void centralizarDoisMarcadores(Marker marker1, Marker marker2){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include( marker1.getPosition());
        builder.include( marker2.getPosition());

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, largura,altura, espacoInterno)
        );
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperaLocalizacaoUsuario();

    }

    public void solicitarRastreio(View view){

        if (cancelarRastreio){
            requisicao.setStatus(Requisicao.STATUS_CANCELADA);
            requisicao.atualizarStatus();

        }else {
            String destino = editDestino.getText().toString();

            if(!destino.equals("") || destino != null){

                Address address = recuperarEndereco(destino);
                if (address != null){
                    final Destino destino1 = new Destino();
                    destino1.setCidade( address.getAdminArea());
                    destino1.setCep( address.getPostalCode());
                    destino1.setBairro( address.getSubLocality());
                    destino1.setRua( address.getThoroughfare());
                    destino1.setNumero( address.getFeatureName());
                    destino1.setLatitude( String.valueOf(address.getLatitude()));
                    destino1.setLongitude( String.valueOf(address.getLongitude()));

                    StringBuilder msg = new StringBuilder();
                    msg.append("Cidade: " + destino1.getCidade());
                    msg.append("\nRua: " + destino1.getRua());
                    msg.append("\nBairro: " + destino1.getBairro());
                    msg.append("\nNúmero: " + destino1.getNumero());
                    msg.append("\nCEP: " + destino1.getCep());

                    AlertDialog.Builder build = new AlertDialog.Builder(this)
                            .setTitle("Confirme seu destino desejado!")
                            .setMessage(msg)
                            .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    salvarRequisicao( destino1 );
                                    cancelarRastreio = true;

                                }
                            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    AlertDialog dialog = build.create();
                    dialog.show();
                }

            }else {
                Toast.makeText(this, "Informe o destino/destino aproximado desejado!", Toast.LENGTH_SHORT).show();
            }
        }





    }

    private void salvarRequisicao(Destino destino){

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);

        Usuario logedUser = UsuarioFirebase.getDadoUsuarioLogado();
        logedUser.setLatitude(String.valueOf(minhaLoc.latitude));
        logedUser.setLongitude(String.valueOf(minhaLoc.longitude));
        requisicao.setPassageiro( logedUser );
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        linearLayoutDestino.setVisibility(View.GONE);
        buttonSolicitarRastreio.setText("Sair");

    }

    private Address recuperarEndereco(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listEnderecos = geocoder.getFromLocationName(endereco, 1);
            if( listEnderecos != null && listEnderecos.size() > 0){
                Address address = listEnderecos.get(0);

                return address;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void recuperaLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                minhaLoc = new LatLng(latitude, longitude);

                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);

                alteraInterfaceStatusRequisicao( statusRequisicao );

                if (statusRequisicao != null && !statusRequisicao.isEmpty()) {

                    if (statusRequisicao.equals(Requisicao.STATUS_VIAGEM)
                            || statusRequisicao.equals(Requisicao.STATUS_FINALIZADA)) {
                        locationManager.removeUpdates(locationListener);
                    }else {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    10000,
                                    10,
                                    locationListener
                            );
                        }
                    }
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair:
                auth.signOut();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initComponents(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Buscar melhor ônibus para o destino");
        setSupportActionBar(toolbar);

        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        buttonSolicitarRastreio = findViewById(R.id.buttonSolicitarRastreio);

        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
}
