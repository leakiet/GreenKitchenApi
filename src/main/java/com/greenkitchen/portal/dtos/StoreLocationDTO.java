package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationDTO {
    private Long id;
    
    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 255, message = "Tên chi nhánh không được quá 255 ký tự")
    private String name;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được quá 500 ký tự")
    private String address;
    
    @NotNull(message = "Latitude không được để trống")
    @DecimalMin(value = "-90.0", message = "Latitude phải từ -90 đến 90")
    @DecimalMax(value = "90.0", message = "Latitude phải từ -90 đến 90")
    private Double latitude;
    
    @NotNull(message = "Longitude không được để trống")
    @DecimalMin(value = "-180.0", message = "Longitude phải từ -180 đến 180")
    @DecimalMax(value = "180.0", message = "Longitude phải từ -180 đến 180")
    private Double longitude;
    
    private Boolean isActive = true;
}


