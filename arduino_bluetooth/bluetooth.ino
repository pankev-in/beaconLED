const int ledPin=13;
String inputString = "";
String BLEString = "";
boolean serialComplete = false;
boolean bleComplete = false;


void setup() {
  pinMode(ledPin,OUTPUT);
  Serial.begin(9600);
  Serial1.begin(9600);
  inputString.reserve(200);
  BLEString.reserve(200);
}

void loop() {
  
  if(serialComplete){
    inputString.remove((inputString.length()-1));
    Serial.print("Send:  ");
    Serial.println(inputString);
    Serial1.print(inputString); 
    inputString = "";
    serialComplete = false;
  }
  
  if(bleComplete){
    int firstcomma = BLEString.indexOf(',');
    int secondcomma = BLEString.indexOf(',',(firstcomma+1));
    int n1 = BLEString.substring(0,firstcomma).toInt();
    int n2 = BLEString.substring((firstcomma+1),secondcomma).toInt();
    int n3 = BLEString.substring((secondcomma+1),BLEString.length()).toInt();
    if(n1>127){digitalWrite(ledPin,HIGH);}
    else{digitalWrite(ledPin,LOW);}
    BLEString = "";
    bleComplete = false;
  }
  
  while(Serial.available() > 0){
    char inChar = (char)Serial.read(); 
    inputString += inChar;
    if (inChar == '#') {
      serialComplete = true;
    } 
  }
  while(Serial1.available() > 0){
    char inChar = (char)Serial1.read(); 
    BLEString += inChar;
    Serial.print(inChar);
    if (inChar == '\n') {
      bleComplete = true;
    } 
  }
}
