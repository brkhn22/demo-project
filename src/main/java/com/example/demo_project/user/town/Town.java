package com.example.demo_project.user.town;

import java.time.LocalDateTime;

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
@Table(name = "Town")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Town {
    
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Created_At")
    private LocalDateTime createdAt;

    @Column(name = "Deleted_At")
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "Region_ID", referencedColumnName = "ID")
    private Region region;
}
