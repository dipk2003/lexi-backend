package com.lexiai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private String phoneNumber;
    private String specialization;
    private String barNumber;
    private Integer yearsOfExperience;
    
    @NotBlank(message = "Firm name is required")
    private String firmName;
    
    @Email(message = "Please provide a valid firm email")
    private String firmEmail;
    
    private String firmPhone;
    private String firmAddress;
    private String firmCity;
    private String firmState;
    private String firmCountry;
    
    // Constructors
    public RegisterRequest() {}
    
    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getBarNumber() { return barNumber; }
    public void setBarNumber(String barNumber) { this.barNumber = barNumber; }
    
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    
    public String getFirmName() { return firmName; }
    public void setFirmName(String firmName) { this.firmName = firmName; }
    
    public String getFirmEmail() { return firmEmail; }
    public void setFirmEmail(String firmEmail) { this.firmEmail = firmEmail; }
    
    public String getFirmPhone() { return firmPhone; }
    public void setFirmPhone(String firmPhone) { this.firmPhone = firmPhone; }
    
    public String getFirmAddress() { return firmAddress; }
    public void setFirmAddress(String firmAddress) { this.firmAddress = firmAddress; }
    
    public String getFirmCity() { return firmCity; }
    public void setFirmCity(String firmCity) { this.firmCity = firmCity; }
    
    public String getFirmState() { return firmState; }
    public void setFirmState(String firmState) { this.firmState = firmState; }
    
    public String getFirmCountry() { return firmCountry; }
    public void setFirmCountry(String firmCountry) { this.firmCountry = firmCountry; }
}
