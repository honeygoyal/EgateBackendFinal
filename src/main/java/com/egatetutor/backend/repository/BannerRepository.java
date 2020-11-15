package com.egatetutor.backend.repository;

import com.egatetutor.backend.model.Banner;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BannerRepository extends CrudRepository<Banner, Long> {
    @Query(value = "SELECT * FROM banner", nativeQuery = true)
    List<Banner> findBannerByImageUrlExists();
}
