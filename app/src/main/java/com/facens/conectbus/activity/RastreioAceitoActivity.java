package com.facens.conectbus.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facens.conectbus.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class RastreioAceitoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button buttonAceitarSolicitacao;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private LatLng localPassageiro;
    private Usuario motorista;
    private Usuario passageiro;
    private String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private String statusRequisicao;
    private Boolean requisicaoAtiva;
    private Destino destino;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rastreio_aceito);

        initComponents();

        if( getIntent().getExtras().containsKey("idRequisicao")
                && getIntent().getExtras().containsKey("motorista")){

            Bundle extras = getIntent().getExtras();
            motorista = (Usuario) extras.getSerializable("motorista");
            localMotorista = new LatLng(
                    Double.parseDouble(motorista.getLatitude()),
                    Double.parseDouble(motorista.getLongitude())
            );
            idRequisicao = extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
            verificaStatusRequisicao();
        }

    }

    private void verificaStatusRequisicao() {

        DatabaseReference requisicoes = firebaseRef.child("requisicoes")
                .child( idRequisicao );
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                requisicao = snapshot.getValue( Requisicao.class);
                if(requisicao!=null){
                    passageiro = requisicao.getPassageiro();
                    localPassageiro = new LatLng(
                            Double.parseDouble(passageiro.getLatitude()),
                            Double.parseDouble(passageiro.getLongitude())
                    );

                    statusRequisicao = requisicao.getStatus();
                    destino = requisicao.getDestino();
                    alteraInterfaceStatusRequisicao(statusRequisicao);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void alteraInterfaceStatusRequisicao(String status){
        switch (status){
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
    }

    private void requisicaoCancelada() {

        Toast.makeText(this, "Usuário desativou o rastreamento em seu dispositivo!",
                Toast.LENGTH_SHORT).show();

        startActivity(new Intent(RastreioAceitoActivity.this, RequisicoesActivity.class));

    }

    private void centralizarMarcador(LatLng local){
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(local, 20)
        );
    }

    private void requisicaoFinalizada() {

        requisicaoAtiva = false;

        if(marcadorMotorista != null){
            marcadorMotorista.remove();
        }

        if(marcadorDestino != null){
            marcadorDestino.remove();
        }

        LatLng localDestino = new LatLng(
          Double.parseDouble(destino.getLatitude()),
          Double.parseDouble(destino.getLongitude())
        );

        adicionarMarcadorDestino(localDestino, "Destino");
        centralizarMarcador(localDestino);

        buttonAceitarSolicitacao.setText("Rastreamento concluído");
    }

    private void requisicaoViagem(){
        buttonAceitarSolicitacao.setText("RASTREAMENTO AINDA ATIVO");

        adicionarMarcadorMotorista(localMotorista, motorista.getNome());
        LatLng localDestino = new LatLng(
                Double.parseDouble(destino.getLatitude()),
                Double.parseDouble(destino.getLongitude())
        );
        adicionarMarcadorDestino(localDestino, "Destino");
        centralizarDoisMarcadores(marcadorMotorista, marcadorDestino);

        iniciarMonitoramento(motorista, localDestino, Requisicao.STATUS_FINALIZADA);


    }

    private void requisicaoAguardando(){
        buttonAceitarSolicitacao.setText("Aceitar solicitação de rastreio");

        adicionarMarcadorMotorista(localMotorista,  motorista.getNome());

        centralizarMarcador(localMotorista);
    }

    private void requisicaoaCaminho(){
        buttonAceitarSolicitacao.setText("rastreamento ativo");

        adicionarMarcadorMotorista(localMotorista, motorista.getNome());
        adicionarMarcadorPassageiro(localPassageiro, passageiro.getNome());
        centralizarDoisMarcadores(marcadorMotorista, marcadorPassageiro);
        iniciarMonitoramento(motorista, localPassageiro, Requisicao.STATUS_VIAGEM);

    }

    private void iniciarMonitoramento(Usuario uOrigem, LatLng localDestino, String status){

        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);

        Circle circle = mMap.addCircle(
                new CircleOptions()
                .center(localDestino)
                .radius(50)
                .fillColor(Color.argb(90, 255,153,0))
                .strokeColor(Color.argb(190,255,152,0))
        );

        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(localDestino.latitude, localDestino.longitude),
                0.05
        );

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(key.equals(uOrigem.getId())){
                    requisicao.setStatus(status);
                    requisicao.atualizarStatus();

                    geoQuery.removeAllListeners();
                    circle.remove();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
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


    public void aceitarSolicitacao(View view){

        requisicao = new Requisicao();
        requisicao.setId( idRequisicao );
        requisicao.setMotorista( motorista );
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

        requisicao.atualizar();

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

    @TargetApi(Build.VERSION_CODES.M)
    private void recuperaLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localMotorista = new LatLng(latitude, longitude);

                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);

                motorista.setLatitude(String.valueOf(latitude));
                motorista.setLongitude(String.valueOf(longitude));
                requisicao.setMotorista( motorista );
                requisicao.atualizarLocalizacaoMotorista();

                alteraInterfaceStatusRequisicao(statusRequisicao);
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

    private void initComponents(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Compartilhar localização");

        buttonAceitarSolicitacao = findViewById(R.id.buttonAceitarSolicitacao);

//        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent i = new Intent(RastreioAceitoActivity.this, RequisicoesActivity.class);
        startActivity(i);

        if(statusRequisicao != null && !statusRequisicao.isEmpty()){
            requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
            requisicao.atualizarStatus();
        }

        return false;
    }

}
