# CBUS - ConectBus

    Integrantes
    
    Nome: Gabriel Henrique Garcia Camargo - RA 190375 / PA038TIN1

    Nome: Marcos Vinicius Scucuglia - RA 171553 / PA038TIN1


#### Funcionalidades

- Cadastro e Login do Usuário;
- Visualização dos ônibus de transporte público e seus itinerários;
- O ônibus selecionado pelo usuário será visto em tempo real no mapa, e o trajeto que será realizado até chegar no ponto de espera do usuário;

O aplicativo tem como foco facilitar e cooperar com aquele que utiliza o transporte público, tendo assim mais informações do ônibus que espera, se está ou não atrasado. Tudo isso com uma interface intuitiva de visualização, o próprio mapa em tempo real.

Configuração do build.gradle(app):

    apply plugin: 'com.android.application'

    android {
        compileSdkVersion 29
        buildToolsVersion "32.0.0"
        defaultConfig {
            applicationId "com.facens.conectbus"
            minSdkVersion 21
            targetSdkVersion 29
            versionCode 1
            versionName "1.0"
            testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        }
        buildTypes {
            release {
                minifyEnabled false
                proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            }
        }
        compileOptions {
            sourceCompatibility 1.8
            targetCompatibility 1.8
        }
    }

    dependencies {
        implementation fileTree(dir: 'libs', include: ['*.jar'])
        implementation 'androidx.appcompat:appcompat:1.0.2'
        implementation 'com.google.android.gms:play-services-maps:16.1.0'
        implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
        testImplementation 'junit:junit:4.12'
        androidTestImplementation 'androidx.test.ext:junit:1.1.0'
        androidTestImplementation 'androidx.test.espresso:espresso-core:3.1.1'

        implementation platform('com.google.firebase:firebase-bom:30.1.0')
        implementation 'com.google.firebase:firebase-analytics'
        implementation 'com.google.android.material:material:1.0.0'

        implementation 'com.google.firebase:firebase-auth'
        implementation 'com.google.firebase:firebase-database'
        implementation 'com.firebase:geofire-java:3.0.0'
    }

    apply plugin: 'com.google.gms.google-services'


Instruções/Requisitos para rodar o projeto:
- Android Studio 
- Java 11
- Realizar o clone do projeto em sua máquina local
- Abrir o projeto no Android Studio e iniciar o app juntamente a um emulador

Detalhes do Emulador utilizado no vídeo de apresentação:

    Type: Phone
    Resolution: 1080 x 1920: 420dpi
    API: 28
    Target: Android 9.0 (Google Play)
    CPU: x86
    Size on Disk: 11GB
    

<span align="center">
    
Tela Inicial

## <img src="https://user-images.githubusercontent.com/94056841/172981625-7a7ee696-5ed9-422a-95ef-1163f1965845.png" width="300px"> 
</span>


<span align="center">
    
Tela Sobre

## <img src="https://user-images.githubusercontent.com/94056841/172981980-991dbf3c-b187-4f6c-961c-a65807a33ead.png" width="300px"> 
</span>


<span align="center">
    
Tela Cadastro

## <img src="https://user-images.githubusercontent.com/94056841/172982029-c953f26c-8472-449b-8a3f-b510ecaf409d.png" width="300px"> 
</span>


<span align="center">
    
Tela Login

## <img src="https://user-images.githubusercontent.com/94056841/172982066-6599684f-7f1c-499f-8e23-f78309259b21.png" width="300px"> 
</span>


<span align="center">
    
Tela Solicita Rastreio de Ônibus próximo

## <img src="https://user-images.githubusercontent.com/94056841/172982109-aa12647f-0ea8-4fcf-8f32-43d60d0c9343.png" width="300px"> 
</span>


<span align="center">
    
Confirmação do Destino Aproximado do Usuário

## <img src="https://user-images.githubusercontent.com/94056841/172982169-e1da01ba-3c1f-465f-98a5-6c5cd93f442e.png" width="300px"> 
</span>

<span align="center">
    
Listagem de pedidos de rastreamento para o motorista do ônibus.

"a" é o nome do usuário que realizou o pedido de rastreamento!

## <img src="https://user-images.githubusercontent.com/94056841/172982229-a7e44496-073b-415d-ac39-b5d7089d5d06.png" width="300px"> 
</span>

<span align="center">
    
Após clicar em algum dos pedidos da tela anterior, exibe a tela de rastreamento ativo.

## <img src="https://user-images.githubusercontent.com/94056841/172982272-c0273883-25d5-4047-8983-c480dfb68b7f.png" width="300px"> 
</span>
