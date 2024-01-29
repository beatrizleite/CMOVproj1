#define trigPin1 10
#define echoPin1 9

/* For the second sensor
#define trigPin2 11
#define trigPin2 12
*/

int count = 0;
int sensor1;
int sensor2;
String valuesReceived = "";

void setup() 
{ 
  Serial.begin(9600);
  sensor1 = getDistance(echoPin1, trigPin1);
  //sensor2 = getDistance(echoPin2, trigPin2);
} 

void loop() 
{ 
	int sensor1NewVal = getDistance(echoPin1, trigPin1);
	//int sensor2NewVal = getDistance(echoPin2, trigPin2);

  /*
If there were 2 sensors:
  if(sensor1 < sensor1NewVal - 30 && valuesReceived.charAt(0) != '1') {
    valuesReceived += '1';
  } else if(sensor2 < sensor2NewVal - 30 && valuesReceived.charAt(0) != '2') {
    valuesReceived += '2';
  }
  */

  //if(sensor1 <= sensor1NewVal) {
  if(count < 7) {  
    if(valuesReceived.charAt(0) != '1') {
      valuesReceived += "1";
    } else if(valuesReceived.charAt(0) != '2') {
      valuesReceived += "2";
    }
  } else if(count > 5) {
    if(valuesReceived.charAt(0) != '2') {
      valuesReceived += "2";
    } else if(valuesReceived.charAt(0) != '1') {
      valuesReceived += "1";
    }
  }

  //verify if values in the string created are equal to 12 or 21 which means somes left or entered the room
  if(valuesReceived.equals("12")) {
    count++;
    valuesReceived = "";
    delay(550);
    printForXbee(count);
  } else if(valuesReceived.equals("21") && count > 0) {
    count--;
    valuesReceived="";
    delay(550);
    printForXbee(count);
  }


  //in case there are errors...
  if(valuesReceived.length() > 2 || valuesReceived.equals("11") || valuesReceived.equals("22")) {
    valuesReceived = "";
  }
  
  delay(10000);
}

void printForXbee(int count) {
  Serial.print("ppl=");
  Serial.print(count);
  Serial.print("\n");
}

// pin1 - echo
// pin2 - trig
int getDistance(int pin1, int pin2) {
  pinMode(pin2, OUTPUT);
  digitalWrite(pin2, LOW);
  delay(200);
  digitalWrite(pin2, HIGH);
  delay(100);
  digitalWrite(pin2, LOW);
  pinMode(pin1, INPUT);
  long duration = pulseIn(pin1, HIGH, 100000);
  return duration / 29 / 2;
}
