package com.kahanchale.traveladviser.repository;

import com.kahanchale.traveladviser.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByNameAndStateAndCountry(String name, String state, String country);
    Optional<City> findByName(String name);
}
