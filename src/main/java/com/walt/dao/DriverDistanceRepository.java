package com.walt.dao;

import com.walt.model.DriverDistanceImpl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverDistanceRepository extends CrudRepository<DriverDistanceImpl, Long> {
}
