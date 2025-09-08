package com.greenkitchen.portal.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenkitchen.portal.entities.Settings;
import com.greenkitchen.portal.repositories.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> getAllSettings() {
        List<Settings> allSettings = settingsRepository.findByIsActiveTrue();
        Map<String, Object> settingsMap = new HashMap<>();

        for (Settings setting : allSettings) {
            try {
                // Try to parse as JSON first, if fails treat as string
                Object value = objectMapper.readValue(setting.getSettingValue(), Object.class);
                settingsMap.put(setting.getSettingKey(), value);
            } catch (JsonProcessingException e) {
                // If not JSON, store as string
                settingsMap.put(setting.getSettingKey(), setting.getSettingValue());
            }
        }

        return settingsMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSettingsByType(String settingType) {
        List<Settings> settings = settingsRepository.findActiveBySettingType(settingType);
        Map<String, Object> settingsMap = new HashMap<>();

        for (Settings setting : settings) {
            try {
                Object value = objectMapper.readValue(setting.getSettingValue(), Object.class);
                settingsMap.put(setting.getSettingKey(), value);
            } catch (JsonProcessingException e) {
                settingsMap.put(setting.getSettingKey(), setting.getSettingValue());
            }
        }

        return settingsMap;
    }

    @Transactional
    public void saveSetting(String key, Object value, String settingType) throws JsonProcessingException {
        String jsonValue = objectMapper.writeValueAsString(value);

        Optional<Settings> existingSetting = settingsRepository.findBySettingKey(key);

        if (existingSetting.isPresent()) {
            Settings setting = existingSetting.get();
            setting.setSettingValue(jsonValue);
            setting.setSettingType(settingType);
            settingsRepository.save(setting);
        } else {
            Settings newSetting = new Settings();
            newSetting.setSettingKey(key);
            newSetting.setSettingValue(jsonValue);
            newSetting.setSettingType(settingType);
            newSetting.setIsActive(true);
            settingsRepository.save(newSetting);
        }
    }

    @Transactional
    public void saveSettingsBulk(Map<String, Object> settings, String settingType) throws JsonProcessingException {
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            saveSetting(entry.getKey(), entry.getValue(), settingType);
        }
    }

    @Transactional
    public void deleteSetting(String key) {
        Optional<Settings> setting = settingsRepository.findBySettingKey(key);
        if (setting.isPresent()) {
            settingsRepository.delete(setting.get());
        }
    }

    @Transactional
    public void deactivateSetting(String key) {
        Optional<Settings> setting = settingsRepository.findBySettingKey(key);
        if (setting.isPresent()) {
            Settings s = setting.get();
            s.setIsActive(false);
            settingsRepository.save(s);
        }
    }
}
