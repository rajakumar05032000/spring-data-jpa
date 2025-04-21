package org.springframework.data.jpa.repository.sample;

import org.springframework.data.jpa.domain.sample.Customer;
import org.springframework.data.jpa.domain.sample.CustomerDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = "select id as Id, name as name from Customer", nativeQuery = true)
    List<CustomerDTO> findByNameNativeQuery2(@Param("name") String name);

    @Query(value = "select id as Id, name as name from Customer where rownum=1", nativeQuery = true)
    CustomerDTO findByNameNativeQuery3(@Param("name") String name);

}
