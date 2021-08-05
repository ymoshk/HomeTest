package com.walt.business.logic;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverDistanceRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DeliveryFactory {
    @Resource
    CustomerRepository customerRepository;
    @Resource
    DriverRepository driverRepository;
    @Resource
    DeliveryRepository deliveryRepository;
    @Resource
    DriverDistanceRepository driverDistanceRepository;

    private Customer customer;
    private Date deliveryTime;

    public Delivery create(Customer customer, Restaurant restaurant, Date deliveryTime) {
        Delivery result = null;

        if (customer.getCity().equals(restaurant.getCity())) {
            this.customer = customer;
            this.deliveryTime = deliveryTime;

            verifyCustomerExistence();
            Driver driver = selectDriver();

            if (driver != null) {
                result = new Delivery(driver, restaurant, customer, deliveryTime);
                saveDelivery(result);
            }
        }

        return result;
    }

    private void saveDelivery(Delivery deliveryToSave) {
        Random random = new Random();
        int distance = random.nextInt(21);
        DriverDistanceImpl driverDistance =
                new DriverDistanceImpl(deliveryToSave.getDriver(), (long) distance);

        deliveryToSave.setDistance(distance);
        this.deliveryRepository.save(deliveryToSave);
        driverDistanceRepository.save(driverDistance);
    }

    private void verifyCustomerExistence() {
        if (this.customerRepository.findByName(this.customer.getName()) != null) {
            this.customerRepository.save(this.customer);
        }
    }

    private List<Driver> getOptionalDrivers() {
        List<Driver> busyDrivers = StreamSupport
                .stream(this.deliveryRepository.findAll().spliterator(), false)
                .filter(delivery -> !isDeliveryDriverAvailable(delivery))
                .map(Delivery::getDriver)
                .collect(Collectors.toList());

        return this.driverRepository
                .findAllDriversByCity(this.customer.getCity())
                .stream()
                .filter(driver -> !busyDrivers.contains(driver))
                .collect(Collectors.toList());
    }

    private Driver selectDriver() {
        Driver result = null;
        List<Driver> driverList = getOptionalDrivers();

        if (!driverList.isEmpty()) {
            if (driverList.size() == 1) {
                result = driverList.get(0);
            } else {
                result = driverList.stream().reduce((driverOne, driverTwo) -> {
                    long driverOneCount = getDriversDeliveriesCount(driverOne);
                    long driverTwoCount = getDriversDeliveriesCount(driverTwo);
                    return driverOneCount <= driverTwoCount ? driverOne : driverTwo;
                }).get();

            }
        }

        return result;
    }

    private long getDriversDeliveriesCount(Driver driver) {
        return StreamSupport.stream(this.deliveryRepository.findAll().spliterator(), false)
                .filter(delivery -> delivery.getDriver().equals(driver))
                .count();
    }

    /**
     * @param deliveryToCheck Delivery to check if it's driver is available.
     * @return true if the new delivery time minus one hour (departure time) is
     * after the supplying time of the deliveryToCheck
     */
    private boolean isDeliveryDriverAvailable(Delivery deliveryToCheck) {
        Date deliveryToCheckTime = deliveryToCheck.getDeliveryTime();
        Date departureTime = Date.from(this.deliveryTime.toInstant().minusSeconds(60 * 60));

        return departureTime.after(deliveryToCheckTime);
    }

}
