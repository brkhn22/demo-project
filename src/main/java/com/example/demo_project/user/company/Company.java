package com.example.demo_project.user.company;

import java.time.LocalDateTime;

import com.example.demo_project.user.company.company_type.CompanyType;
import com.example.demo_project.user.town.Town;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Company")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Short_Name")
    private String shortName;

    @ManyToOne
    @JoinColumn(name = "Company_Type_ID", referencedColumnName = "ID")
    private CompanyType type;

    @ManyToOne
    @JoinColumn(name = "Town_ID", referencedColumnName = "ID")
    private Town town;
    
    @Column(name = "Address_Detail")
    private String address;

    @Column(name = "Active")
    private Boolean active;
    @Column(name = "Created_At")
    private LocalDateTime createdAt;
    @Column(name = "Deleted_At")
    private LocalDateTime deletedAt;
}
