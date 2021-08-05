package com.walt.model;

import javax.persistence.*;

@Entity
public class DriverDistanceImpl implements DriverDistance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Driver driver;
    private Long distance;

    public DriverDistanceImpl(Driver driver, Long distance) {
        this.driver = driver;
        this.distance = distance;
    }

    public DriverDistanceImpl() {
    }

    @Override
    public Driver getDriver() {
        return this.driver;
    }

    @Override
    public Long getTotalDistance() {
        return this.distance;
    }
}
