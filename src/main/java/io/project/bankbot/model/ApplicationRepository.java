package io.project.bankbot.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApplicationRepository extends CrudRepository<Application, Long> {

    Application findByApplicationOwner(Long applicationOwner);

    Application findByApplicationId(Long applicationId);

    Application findByApplicationOwnerAndApplicationAmount(Long applicationOwner, Long applicationAmount);

    Application findByApplicationOwnerAndApplicationInvestor(Long applicationOwner, Long applicationInvestor);

    @Query("SELECT a FROM applicationsDataTable a WHERE (a.applicationOwner = ?1 OR a.applicationInvestor = ?2) AND a.applicationStatus != 'consideration'")
    List<Application> findAllByApplicationOwnerOrApplicationInvestorAndStatusNot(Long applicationOwner, Long applicationInvestor);

    @Query("SELECT a FROM applicationsDataTable a WHERE (a.applicationOwner = ?1 OR a.applicationInvestor = ?2) AND a.applicationStatus = 'consideration'")
    List<Application> findAllByApplicationOwnerOrApplicationInvestorAndStatus(Long applicationOwner, Long applicationInvestor);

    @Query("SELECT a FROM applicationsDataTable a WHERE a.applicationInvestor = ?1 AND a.applicationStatus = 'consideration'")
    List<Application> findAllByApplicationInvestorAndStatus(Long applicationInvestor);

    @Query("SELECT a FROM applicationsDataTable a WHERE a.applicationOwner = ?1 AND a.applicationStatus = 'consideration'")
    List<Application> findAllByApplicationOwnerAndStatus(Long applicationOwner);

    @Query("SELECT a FROM applicationsDataTable a WHERE a.applicationOwner = ?1 AND a.applicationStatus = 'waiting_on_refunds'")
    List<Application> findAllByApplicationOwnerAndStatusWaiting(Long applicationOwner);
}

