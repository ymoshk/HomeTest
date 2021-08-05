package com.walt;

import com.walt.business.logic.DeliveryFactory;
import com.walt.business.logic.StatisticsProvider;
import com.walt.dao.DriverDistanceRepository;
import com.walt.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;


public class DeliveriesTests extends WaltTest {
    final int ITERATIONS = 100;
    @Autowired
    DeliveryFactory deliveryFactory;
    @Autowired
    DriverDistanceRepository driverDistanceRepository;
    @Autowired
    StatisticsProvider statisticsProvider;


    @Test
    public void create() {
        assertEquals(this.deliveryRepository.count(), 0);

        Customer customer = this.customerRepository.findByName("Beethoven");
        Restaurant restaurant = this.restaurantRepository.findByName("vegan");
        Delivery delivery = this.deliveryFactory.create(customer, restaurant, new Date());

        assertEquals(this.deliveryRepository.count(), 1);
        assertEquals(delivery.getDriver().getCity(), customer.getCity());
    }

    @Test
    public void testNoAvailableDriver() {
        // Only 3 drivers are located in TLV
        Restaurant restaurant = this.restaurantRepository.findByName("vegan");
        Customer customer = this.customerRepository.findByName("Beethoven");

        Delivery deliveryOne = deliveryFactory.create(customer, restaurant, new Date());
        Delivery deliveryTwo = deliveryFactory.create(customer, restaurant, new Date());
        Delivery deliveryThree = deliveryFactory.create(customer, restaurant, new Date());
        Delivery deliveryFour = deliveryFactory.create(customer, restaurant, new Date());

        assertNotNull(deliveryOne);
        assertNotNull(deliveryTwo);
        assertNotNull(deliveryThree);

        assertNull(deliveryFour);
    }

    @Test
    public void testDriverAvailableAfterDeliveryCompletion() {
        // Only 3 drivers are located in TLV
        Restaurant restaurant = this.restaurantRepository.findByName("vegan");
        Customer customer = this.customerRepository.findByName("Beethoven");

        Delivery deliveryOne = deliveryFactory.create(customer, restaurant, new Date());
        Delivery deliveryTwo = deliveryFactory.create(customer, restaurant, new Date());
        Delivery deliveryThree = deliveryFactory.create(customer, restaurant, new Date());

        Delivery deliveryFour = deliveryFactory.create(customer, restaurant,
                Date.from(Instant.now().plusSeconds(60 * 59)));

        Delivery deliveryFive = deliveryFactory.create(customer, restaurant,
                Date.from(Instant.now().plusSeconds(60 * 60)));

        assertNotNull(deliveryOne);
        assertNotNull(deliveryTwo);
        assertNotNull(deliveryThree);
        assertNotNull(deliveryFive);

        assertNull(deliveryFour);
    }

    /**
     * The test checks whether the least busy driver will always be chosen.
     * In each iteration it produce a new delivery one hour apart from the previous one
     * so that all drivers are necessarily available at the time the delivery is created.
     * The amount of deliveries that each driver made is kept on a hash map and in each iteration
     * we make sure that the selected driver has done a lower number of deliveries than the rest.
     */
    @Test
    public void testLeastBusyDriverSelected() {
        Restaurant restaurant = this.restaurantRepository.findByName("vegan");
        Customer customer = this.customerRepository.findByName("Beethoven");

        Map<Driver, Integer> deliveriesCountMap = new HashMap<>();

        for (int i = 0; i < this.ITERATIONS; i++) {
            Delivery delivery = this.deliveryFactory.create(customer, restaurant,
                    Date.from(Instant.now().plusSeconds(60 * 60 * i)));

            if (!deliveriesCountMap.containsKey(delivery.getDriver())) {
                deliveriesCountMap.put(delivery.getDriver(), 0);
            }

            int min = deliveriesCountMap.values().stream().min(Integer::compareTo).get();

            List<Driver> leastBusyDrivers = deliveriesCountMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == min)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            assertTrue(leastBusyDrivers.contains(delivery.getDriver()));

            deliveriesCountMap.put(delivery.getDriver(),
                    deliveriesCountMap.get(delivery.getDriver()) + 1);
        }
    }

    @Test
    public void testDriverDistanceCreation() {
        Restaurant restaurant = this.restaurantRepository.findByName("vegan");
        Customer customer = this.customerRepository.findByName("Beethoven");
        assertEquals(this.driverDistanceRepository.count(), 0);

        this.deliveryFactory.create(customer, restaurant, new Date());
        assertEquals(this.driverDistanceRepository.count(), 1);
    }


    @Test
    public void testDriverDistanceReport() {
        Map<Driver, Long> distancesMap = buildDistancedMap();
        List<DriverDistance> driverDistances;

        driverDistances = this.statisticsProvider.getTotalDistancePerDriver();

        for (Driver driver : this.driverRepository.findAll()) {
            driverDistances
                    .stream()
                    .filter(driverDistance -> driverDistance.getDriver().equals(driver))
                    .reduce((driverDistance, driverDistance2) ->
                            new DriverDistanceImpl(driver, driverDistance.getTotalDistance() +
                                    driverDistance2.getTotalDistance()))
                    .ifPresent(currentDriverDistance ->
                            assertEquals(currentDriverDistance.getTotalDistance(), distancesMap.get(driver)));
        }
    }


    private Map<Driver, Long> buildDistancedMap() {
        Map<Driver, Long> distancesMap = new HashMap<>();

        for (int i = 0; i < this.ITERATIONS; i++) {
            Delivery delivery = createRandomDelivery();
            if (delivery != null) {
                Driver driver = delivery.getDriver();
                Long distance = new Double(delivery.getDistance()).longValue();

                if (!distancesMap.containsKey(driver)) {
                    distancesMap.put(driver, 0L);
                }
                distancesMap.put(driver, distancesMap.get(driver) + distance);
            }
        }

        return distancesMap;
    }

    private List<? extends NamedEntity> repositoryToList(
            CrudRepository<? extends NamedEntity, Long> repository) {
        return StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    private Delivery createRandomDelivery() {
        List<Customer> customers = (List<Customer>) repositoryToList(this.customerRepository);
        List<Restaurant> restaurants = (List<Restaurant>) repositoryToList(this.restaurantRepository);
        Random random = new Random();
        int index = random.nextInt(Math.max((int) this.customerRepository.count() - 1, 1));
        Customer customer = customers.get(index);

        List<Restaurant> restaurantsInCustomerCity = restaurants
                .stream()
                .filter(restaurant -> customer.getCity().equals(restaurant.getCity()))
                .collect(Collectors.toList());

        int restaurantIndex = random.nextInt(Math.max(restaurantsInCustomerCity.size() - 1, 1));

        Restaurant restaurant = restaurants.get(restaurantIndex);

        int randomHour = random.nextInt(10);

        return this.deliveryFactory.create(customer, restaurant,
                Date.from(Instant.now().plusSeconds(60 * 60 * randomHour)));
    }
}