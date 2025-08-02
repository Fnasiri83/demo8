package com.example.demo.dto;



public class FormReportDto {
    private String description;
   private String incidentType;

    private String phone;


    private String address;

    private  Double longitude;
    private Double latitude;


      private  String username;
      public FormReportDto() {
          super();
      }
      public void setDescription(String description) {
          this.description = description;
      }
      public void setIncidentType(String incidentType) {
          this.incidentType = incidentType;
      }
      public void setAddress(String address) {
          this.address = address;
      }
      public void setLongitude(Double longitude) {
          this.longitude = longitude;
      }
      public void setLatitude(Double latitude) {
          this.latitude = latitude;
      }
      public void setPhone(String phone) {
          this.phone = phone;
      }
      public void setUsername(String username) {
          this.username = username;
      }
      public String getDescription() {
          return description;
      }
      public String getIncidentType() {
          return incidentType;
      }
      public String getAddress() {
          return address;
      }
      public Double getLongitude() {
          return longitude;
      }
      public Double getLatitude() {
          return latitude;
      }
      public String getPhone() {
          return phone;
      }
      public String getUsername() {
          return username;
      }


}
