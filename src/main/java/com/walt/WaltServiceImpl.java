package com.walt;

import com.walt.business.logic.DeliveryFactory;
import com.walt.business.logic.StatisticsProvider;
import com.walt.model.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WaltServiceImpl implements WaltService {
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        Delivery delivery = new DeliveryFactory().create(customer, restaurant, deliveryTime);

        if (delivery == null) {
            System.out.println("Delivery cannot be completed because no driver is available. Try again later.");
        }

        return delivery;
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        return new StatisticsProvider().getTotalDistancePerDriver();
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return new StatisticsProvider().getTotalDistancePerDriver(city);
    }
}
