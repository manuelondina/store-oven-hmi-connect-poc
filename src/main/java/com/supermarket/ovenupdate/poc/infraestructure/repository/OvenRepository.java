package com.supermarket.ovenupdate.poc.infraestructure.repository;

import com.supermarket.ovenupdate.poc.domain.Oven;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OvenRepository extends JpaRepository<Oven, Long>, JpaSpecificationExecutor<Oven> {

    List<Oven> findByNameContainsIgnoreCase(String name);

    List<Oven> findByLocationContainsIgnoreCase(String location);

    Optional<Oven> findByIpAddress(String ip);

}
