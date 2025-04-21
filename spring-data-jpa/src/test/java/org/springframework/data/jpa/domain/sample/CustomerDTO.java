package org.springframework.data.jpa.domain.sample;

import jakarta.persistence.Id;

public class CustomerDTO {

    Long id;

    String name;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

//public record CustomerDTO(Long id, String name) {
//}
