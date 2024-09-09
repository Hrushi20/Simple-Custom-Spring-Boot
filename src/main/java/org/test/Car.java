package org.test;

import org.test.annotation.Autowired;
import org.test.annotation.Component;
import org.test.carParts.Engine;

@Component
public class Car extends Process {
    private Engine engine;
    private String carName;

    @Autowired
    public Car(Engine engine, String carName){
        this.engine = engine;
        this.carName = carName;
    }

    public void startCar(){
        System.out.println("Car Name: " + carName);
        engine.startEngine();
    }

    public void stopEngine(){
        engine.stopEngine();
    }

    @Override
    void startProcess() {
        startCar();
    }

    @Override
    void stopProcess() {
        stopEngine();
    }


}
