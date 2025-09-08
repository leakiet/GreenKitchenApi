package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.services.SettingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/apis/v1/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSettings() {
        Map<String, Object> settings = settingsService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/type/{settingType}")
    public ResponseEntity<Map<String, Object>> getSettingsByType(@PathVariable("settingType") String settingType) {
        Map<String, Object> settings = settingsService.getSettingsByType(settingType);
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveSetting(@RequestBody Map<String, Object> request) {
        try {
            String key = (String) request.get("key");
            Object value = request.get("value");
            String settingType = (String) request.get("settingType");

            if (key == null || value == null || settingType == null) {
                return ResponseEntity.badRequest().body("Missing required parameters: key, value, settingType");
            }

            settingsService.saveSetting(key, value, settingType);
            return ResponseEntity.ok("Setting saved successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving setting: " + e.getMessage());
        }
    }

    @PostMapping("/save-bulk")
    public ResponseEntity<String> saveSettingsBulk(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> settings = (Map<String, Object>) request.get("settings");
            String settingType = (String) request.get("settingType");

            if (settings == null || settingType == null) {
                return ResponseEntity.badRequest().body("Missing required parameters: settings, settingType");
            }

            settingsService.saveSettingsBulk(settings, settingType);
            return ResponseEntity.ok("Settings saved successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving settings: " + e.getMessage());
        }
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> deleteSetting(@PathVariable("key") String key) {
        try {
            settingsService.deleteSetting(key);
            return ResponseEntity.ok("Setting deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting setting: " + e.getMessage());
        }
    }

    @PutMapping("/{key}/deactivate")
    public ResponseEntity<String> deactivateSetting(@PathVariable String key) {
        try {
            settingsService.deactivateSetting(key);
            return ResponseEntity.ok("Setting deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deactivating setting: " + e.getMessage());
        }
    }
}
