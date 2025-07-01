package com.one.what2do.entities;

import com.one.what2do.entities.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;
    private String email;
    private String gender;
    private Integer age;
    private String address;
    
    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_my_data_consented")
    private boolean isMyDataConsented;

    @Builder
    public User(String username, String name, String email, String gender, Integer age, String address, boolean isMyDataConsented) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.isMyDataConsented = isMyDataConsented;
    }
}
