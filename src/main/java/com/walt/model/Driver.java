package com.walt.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class Driver extends NamedEntity {

    @ManyToOne
    City city;

    public Driver() {
    }

    public Driver(String name, City city) {
        super(name);
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        Driver driver = (Driver) o;
        return city.equals(driver.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), city);
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
