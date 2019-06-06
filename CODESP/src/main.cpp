#include <Arduino.h>

#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <PubSubClient.h>
#include <ESP8266HTTPClient.h>
#include "ArduinoJson.h"
#include <Time.h>
#include <NTPtimeESP.h>
#include "EmonLib.h"  //Libreria del sensor SCT-013

//Crear una instancia EnergyMonitor

EnergyMonitor energyMonitor;

//Voltaje de nuestra red electrica
float voltajeRed = 220.0;
HTTPClient client2;
int potenciaSimulada;
int estado=0;//Suponer que está apagado en el incio
int controlParental=0;
int fechaFin=0;
const int rele = 16;
struct tm *tm;
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "europe.pool.ntp.org", 3600, 60000);
strDateTime dateTime;
NTPtime NTPch("ch.pool.ntp.org");



//Red wifi
const char* ssid = "jess";
//Contraseña de la red
const char* password ="a94e40b7bbbc";
//nombre del canal
const char* channel_name = "topic_ESP";
//servidor mqtt
//tenemos que hacer un ipconfig para ver la ip
const char* mqtt_server = "192.168.43.78";
//servidor http
const char* http_server = "192.168.43.78";
//Puerto sobre el que va rest
const char* http_server_port = "8081";
//Variable para que el cliente se identifique
String clientId;
//Variable para conectarnos al canal mqtt
WiFiClient espClient;
PubSubClient client(espClient);
//Nos permite publicar cada 2 segundos un mensaje en el canal mqtt
long lastMsg = 0;
long lstMsgRest =0;
//Variable para almacenar el mensaje que nos envia el cliente desde mqtt
char msg[50];
//Disponibilidad del Servicio
int value = 0;
int relecounter = 0;

void desactivarControlParental();
void activarControlParental();
//COnectamos el ESP8266 al wifi
void setup_wifi(){
  delay(10);
  randomSeed(micros());
  Serial.println();
  Serial.print("Conectando a la red WiFi");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while(WiFi.status() != WL_CONNECTED){
    Serial.print(".");
    delay(500);
  }

  //Aquí ya estamos conectados
  Serial.println("");
  Serial.println("WiFi conectado");
  Serial.print("Dirección IP registrada: ");
  Serial.println(WiFi.localIP());
}

//Va a ser llamado cuando ocurra un HANDLE
void callback(char* topic, byte* payload, unsigned length){
  Serial.print("Mensaje recibido [canal: ");
  Serial.print(topic);
  Serial.println("]");
  for (int i = 0; i < length; i++) {
   Serial.print((char)payload[i]);
 }
 Serial.println();


 DynamicJsonDocument doc(length);
 deserializeJson(doc, payload, length);
 const char* action = doc["action"];
 const int idPlaca = doc["idPlaca"];

 if(idPlaca == 1){
 Serial.printf("Acción recibida mqtt %s\n", action);
 // Encendemos un posible switch digital (un diodo led por ejemplo) si el
 // contenido del cuerpo es 'on'
 if (strcmp(action, "ON") == 0) {
   fechaFin = doc["fechaFin"];
   digitalWrite(rele, LOW);
   activarControlParental();
   const char* fechaDesactivarCP = "27-05-2019,21:00:40";
   //const char* fechaDesactivarCP = "27-05-2019,21:00:40";


   Serial.println("RELE DESACTIVADO");
   controlParental=1;

 } else if (strcmp(action, "OFF") == 0) {

   digitalWrite(rele, HIGH);
   Serial.println("RELE ACTIVADO");
   controlParental=0;
   desactivarControlParental();

 } else{
   Serial.println("Acción no reconocida");
 }
}

}
void activarControlParental(){
  const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
  Serial.println("-------Llamada al servidor para registrar CP ACTIVO--------");
  client2.begin("http://192.168.43.78:8090//ActivacionControlParental");
  DynamicJsonDocument root(bufferSize);
  root["id"] = 1;
	root["activado"]= 1;
  String fd = String(dateTime.day);
  String fm = String(dateTime.month);
  String fy = String(dateTime.year);
  String fh = String(dateTime.hour);
  String fmi = String(dateTime.minute);

  String fechaInicioFormateada = String(fd + "-" + fm + "-" + fy + "," + fh + ":" + fmi + ":00");
  Serial.println(fechaInicioFormateada);

  String ffd = String(day(fechaFin));
  String ffm = String(month(fechaFin));
  String ffy = String(year(fechaFin));
  String ffh = String(hour(fechaFin));
  String ffmi = String(minute(fechaFin));

  String fechaFinFormateada = String(ffd + "-" + ffm + "-" + ffy + "," + ffh + ":" + ffmi + ":00");

  root["fI"] = fechaInicioFormateada;
	root["fF"] = fechaFinFormateada;


  String json_string;
  serializeJson(root, json_string);

  int httpCode = client2.POST(json_string);
    if(httpCode > 0){
      String payload = client2.getString();
      Serial.println(payload);
      Serial.println("------------------OK-------------------");
      Serial.println("Control Parental activado");
      digitalWrite(rele,LOW);
      controlParental=1;


    }else{
      Serial.println("Error no se ha podido activar el Control Parental");
    }
}
void desactivarControlParental(){
  const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
  Serial.println("-------Llamada al servidor para registrar CP INACTIVO--------");
  client2.begin("http://192.168.43.78:8090//DesactivacionControlParental");
  DynamicJsonDocument root(bufferSize);
  root["id"] = 1;

  String json_string;
  serializeJson(root, json_string);

  int httpCode = client2.POST(json_string);
    if(httpCode > 0){
      String payload = client2.getString();
      Serial.println(payload);
      Serial.println("------------------OK-------------------");
      digitalWrite(rele,HIGH);
      controlParental=0;
      Serial.println("Control Parental desactivado");



    }else{
      Serial.println("Error no se ha podido desactivar el Control Parental");
    }
}
void encender(){
  const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;

  client2.begin("http://192.168.43.78:8090//encendido");
  DynamicJsonDocument root(bufferSize);
  root["id"] = 1;
  Serial.println("Realizando petición a servidor (Disp.encendido)");
  String json_string;
  serializeJson(root, json_string);

  int httpCode = client2.POST(json_string);
    if(httpCode > 0){
      String payload = client2.getString();
      Serial.println(payload);
      Serial.println("------------------OK-------------------");
      estado=1;



    }else{
      Serial.println("Error no se ha podido encender");
    }
}
void apagar(){
  const size_t bufferSize = JSON_OBJECT_SIZE(1) + 370;
  Serial.println("Realizando petición al servidor (Disp. apagado)");
  client2.begin("http://192.168.43.78:8090//apagado");
  DynamicJsonDocument root(bufferSize);
  root["id"] = 1;

  String json_string;
  serializeJson(root, json_string);

  int httpCode = client2.POST(json_string);
    if(httpCode > 0){
      String payload = client2.getString();
      Serial.println(payload);
      Serial.println("---------------OK----------------------");
      estado=0;



    }else{
      Serial.println("Error no se ha podido apagar");
    }
}

//Conectarnos al servidor mqtt
void reconnect(){
  while(!client.connected()){
    Serial.println("Conectando al servidor MQTT...");
    clientId = "ESP8266Client-";
    clientId+= String(random(0xffff), HEX);

    if(client.connect(clientId.c_str())){
        String printLine = "  Cliente " + clientId + " conectando al servidor " + mqtt_server;
        Serial.println(printLine);
        String msgWelcome = "Dispositivo con ID = " + clientId + " conectado al canal " +  channel_name;
        //Un nuevo cliente suscrito a este canal
        client.publish(channel_name, msgWelcome.c_str());
        client.subscribe(channel_name);
    }else{
      Serial.print("Error al conectar al canal, rc=");
      Serial.println(client.state());
      Serial.println(". Intentando en 5 segundos");

      delay(5000);
    }

  }
}


void setup() {
//CONFIRGURAMOS EL PIN DEL RELE COMO SALIDA
   pinMode(rele,OUTPUT);
   digitalWrite(rele,HIGH);

   timeClient.begin();




  // Iniciamos la clase indicando
  // Número de pin: donde tenemos conectado el SCT-013
  // Valor de calibración: valor obtenido de la calibración teórica
  energyMonitor.current(0, 9.6);

  pinMode(BUILTIN_LED, OUTPUT);
  Serial.begin(115200);
  Serial.println("Funciona");
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);

}

void loop() {
  //int r=digitalRead(rele);
  //if (r==LOW && controlParental==1) {
    //activarControlParental();

//}else{

//}
  //digitalWrite(rele,HIGH);
  //delay(10000);

//Serial.print(hour(t));

//DESDE AQUI SENSOR SCT-013************
// Obtenemos el valor de la corriente eficaz
// Pasamos el número de muestras que queremos tomar
//double Irms = energyMonitor.calcIrms(1484);
double Irms = energyMonitor.calcIrms(2000);
// Calculamos la potencia aparente
double potencia =  Irms * voltajeRed;
dateTime = NTPch.getNTPtime(1.0, 1);

int horaFin=hour(fechaFin);
horaFin=horaFin+2;
int minutoFin=minute(fechaFin);
int diaFin = day(fechaFin);
int mesFin = month(fechaFin);



if((dateTime.minute == minutoFin) && (dateTime.hour == horaFin)&&(dateTime.day == diaFin)&&(dateTime.month ==mesFin) && controlParental==1){
  Serial.println("Desactivar Control Parental");
  desactivarControlParental();
}


// Mostramos la información por el monitor serie
Serial.println("Potencia =");
Serial.println(potencia);


//Medidas del sensor STC
if(potencia> 70.0 && estado==0) {
   encender();
   estado=1;
}else if(potencia < 20.0 && estado==1){
   apagar();
   estado=0;
}


delay(500);


if(!client.connected()){
    reconnect();

  }

client.loop();
}
