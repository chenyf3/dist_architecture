package com.xpay.demo.web.excel;

import com.alibaba.excel.annotation.ExcelProperty;

public class DataModel {
    @ExcelProperty(value = "地址")
    private String address;
    @ExcelProperty(value = "姓名")
    private String name;
    @ExcelProperty(value = "年龄")
    private Integer age;
    @ExcelProperty(value = "邮箱")
    private String email;
    @ExcelProperty(value = "身高")
    private Integer height;
    @ExcelProperty(value = "性别")
    private Integer sex;
    @ExcelProperty(value = "薪资")
    private Double salary;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
}
