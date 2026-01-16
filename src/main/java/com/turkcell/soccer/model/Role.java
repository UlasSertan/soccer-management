package com.turkcell.soccer.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name= "roles")
public class Role {

    public enum RoleName {
        USER,
        ADMIN;

        public String authority() {
            return "ROLE_" + this.name();
        }
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission", // Table that is going to be fetched
            joinColumns = @JoinColumn(name = "role_id"), // Foreign key for role
            inverseJoinColumns = @JoinColumn(name = "permission_id") // Foreign key for permission
    )
    private Set<Permission> permissions;

}
