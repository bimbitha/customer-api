package com.example.customerapi.dto;

public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private Long age;
    private Long phoneNumber;
    private String address;
    private String status;
    private String customerType;

    public CustomerResponse() {
    }

    public CustomerResponse(
            Long id,
            String name,
            String email,
            Long age,
            Long phoneNumber,
            String address,
            String status,
            String customerType) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.status = status;
        this.customerType = customerType;
    }
// CustomerResponse(Long, String, String, Long, Long, String, String, String) is undefinedJava(134217858)
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Long getAge() {
        return age;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerType() {
        return customerType;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public void setphoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

   
}