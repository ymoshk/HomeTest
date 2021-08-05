package com.walt.business.logic;

import com.walt.dao.DriverDistanceRepository;
import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.DriverDistance;
import com.walt.model.DriverDistanceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsProvider {

    @Resource
    private DriverDistanceRepository driverDistanceRepository;

    public List<DriverDistance> getTotalDistancePerDriver() {
        return getTotalDistancePerDriver(null);
    }

    public List<DriverDistance> getTotalDistancePerDriver(City city) {
        HashMap<Driver, Long> driversTotalDistance = new HashMap<>();

        this.driverDistanceRepository.findAll().forEach(driverDistance -> {
            if (city == null || driverDistance.getDriver().getCity().equals(city)) {

                if (!driversTotalDistance.containsKey(driverDistance.getDriver())) {
                    driversTotalDistance.put(driverDistance.getDriver(), 0L);
                }

                Long currentDistance = driversTotalDistance.get(driverDistance.getDriver());

                driversTotalDistance.put(driverDistance.getDriver(),
                        currentDistance + driverDistance.getTotalDistance());
            }
        });

        return driversTotalDistance.entrySet()
                .stream()
                .map(entry -> new DriverDistanceImpl(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
