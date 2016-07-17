package com.example.hp.thetacab;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by hp on 7/17/2016.
 */
public class FairCalculation {
    int cabType;
    int bikeBaseRate;
    int bikeDistanceCoefficient;
    int bikeTimeCoefficient;
    int carBaseRate;
    int carDistanceCoefficent;
    int carTimeCoefficient;

    String bikeBaseRateKey = "bikeBaseRate";
    String bikeDistanceCoefficientKey = "bikeDistanceCoefficient";
    String bikeTimeCoefficientKey = "bikeTimeCoefficient";
    String carBaseRateKey = "carBaseRate";
    String carDistanceCoefficientKey = "carDistanceCoefficient";
    String carTimeCoefficientKey = "carTimeCoefficient";

    public FairCalculation(int cabType){
        this.cabType = cabType;
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(bikeBaseRateKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    bikeBaseRate = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(bikeDistanceCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    bikeDistanceCoefficient = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(bikeTimeCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    bikeTimeCoefficient = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(carBaseRateKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    carBaseRate = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(carDistanceCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    carDistanceCoefficent =Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("FairCalculationParams").child(carTimeCoefficientKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    carTimeCoefficient = Integer.valueOf(String.valueOf((Long) dataSnapshot.getValue()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public int getFairEstimate(int totalDistanceInM, int etaInSeconds){
        double distanceInKm = ((double)totalDistanceInM)/((double) 1000);
        int KMs = (int) (distanceInKm+0.5);
        double etaInMinutes = ((double) etaInSeconds)/((double)60);
        int Mins = (int) (etaInMinutes + 0.5);

        if(cabType == 0){
            if(KMs<=5){
                return (5*bikeDistanceCoefficient)+(Mins*bikeTimeCoefficient)+bikeBaseRate;
            }else if(KMs>=20){
                return (20*bikeDistanceCoefficient)+(Mins*bikeTimeCoefficient)+bikeBaseRate;
            }else {
                return (KMs*bikeDistanceCoefficient)+(Mins*bikeTimeCoefficient)+bikeBaseRate;
            }
        }else if(cabType ==1){
            return (KMs*carDistanceCoefficent)+(Mins*carTimeCoefficient)+carBaseRate;
        }else {
            return -1;
        }
    }

    public void setCabType(int cabType){
        this.cabType = cabType;
    }
}
