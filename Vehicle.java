import java.util.ArrayList;
class Hello{
  public static void main(String []args){
    
  }
}
abstract class Vehicle{
  String number;

  Vehicle(String number){
    this.number=number;
  }


  abstract int calculateFee(int hours);
}

class Car extends Vehicle{
  Car(String number){
    super(number);
  }

  
  int calculateFee(int hours){
    return hours*50;
  }
}

class Bike extends Vehicle{
  Bike(String number){
    super(number);
  }

  
  int calculateFee(int hours){
    return hours*20;
  }
}

class ParkingSlot{
  int slotNumber;
  Vehicle vehicle;


  ParkingSlot(int slotNumber){
    this.slotNumber=slotNumber;
  }
  boolean isEmpty(){
    return vehicle==null;
  }
}

class ParkingLot{
  ArrayList<ParkingSlot> slots=new ArrayList<>();


  ParkingLot(int size){
    for(int i=1;i<=size;i++){
      slots.add(new ParkingSlot(i));
    }
  }


void parkVehicle(Vehicle vehicle){
  for(ParkingSlot slot: slots){
    if(slot.isEmpty()){
      slot.vehicle=vehicle;

    }
  }
  System.out.println(vehicle.number + " parked at slot " + slot.slotNumber);
  return ;
}}