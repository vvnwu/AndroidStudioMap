#include <ESP8266WiFi.h>
#include <FS.h>

// Used the local WiFi settings to set up a hotspot WiFi for the Arduino
const char* ssid = "EY wavespace";
const char* password = "S0ciety@920";
 
int ledPin = 13; // GPIO13---D7 of NodeMCU
WiFiServer server(80);
int count = 0;
int hold = 5000;

bool flag = false;

void setup() {
  Serial.begin(115200);
  delay(10);
  SPIFFS.begin();
  SPIFFS.format();
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
 
  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
 
  WiFi.begin(ssid, password);
    
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
 
  // Start the server
  server.begin();
  Serial.println("Server started");
 
  // Print the IP address
  Serial.print("Use this URL to connect: ");
  Serial.print("http://");
  Serial.print(WiFi.localIP());
  Serial.println("/");
 
}
 
void loop() {
  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }
 
  // Wait until the client sends some data
  Serial.println("new client");
  while(!client.available()){
    delay(1);
  }
  
  // Read the first line of the request
  //Serial.println(client.readStringUntil('/r'));

  // Notify user that their device has connected
  Serial.println("Connected!");

  // Store the HTTP request as a string
  String request = client.readString();

  // Open existing text file to write into
  File f = SPIFFS.open("/refugee.txt", "w");

  // Send the count of connected devices to authorized user
  Serial.println("==Sending to ==");
  if (f) {
    count += 1;
    f.print(request); // Write to text file
    f.close();
  }  
 
  client.flush();
  // Turn on the LED to signal data has been transferred
  digitalWrite(ledPin, HIGH);
  flag = true;
  delay(hold); // wait 5 seconds to allow user to see the LED turning on and off
 
  // Return the response
 
  if(flag) {
     client.println("HTTP/1.1 200 OK");
     client.println("Content-Type: text/html");
     client.println(""); //  do not forget this one
     client.println("<!DOCTYPE HTML>");
     client.println("<html>");
      
    client.println("Location: Received!");
    client.println("<br/>");
    client.println(count);
    client.println("</html>");
    
    client.stop();    
  }
  
  delay(100);
  Serial.println("Client disconnected");
  Serial.println("");
  
  
 
}
