package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {

    Optional<Settings> findBySettingKey(String settingKey);

    List<Settings> findBySettingType(String settingType);

    List<Settings> findByIsActiveTrue();

    @Query("SELECT s FROM Settings s WHERE s.settingType = :settingType AND s.isActive = true")
    List<Settings> findActiveBySettingType(@Param("settingType") String settingType);

    boolean existsBySettingKey(String settingKey);
}
