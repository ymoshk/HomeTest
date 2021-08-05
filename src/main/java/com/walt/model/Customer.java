package com.walt.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class Customer extends NamedEntity {

    @ManyToOne
    City city;
    String address;

    public Customer() {
    }

    public Customer(String name, City city, String address) {
        super(name);
        this.city = city;
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        Customer customer = (Customer) o;
        return city.equals(customer.city) &&
                address.equals(customer.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), city, address);
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
