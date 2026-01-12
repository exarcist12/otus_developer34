package ru.otus.crm.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client")
public class Client implements Cloneable {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id")
    @MapsId
    private Address address;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Phone> phones = new ArrayList<>();

    public Client(String name) {
        this.id = null;
        this.name = name;
    }

    public Client(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Client(Long id, String name, Address address, List<Phone> phones) {

        this.id = id;
        this.name = name;

        if (address != null) {
            this.address = address;
            address.setClient(this);
        }

        this.phones = new ArrayList<>();
        if (phones != null) {
            this.phones.addAll(phones);
            this.phones.forEach(phone -> phone.setClient(this));
        }
    }

    @Override
    @SuppressWarnings({"java:S2975", "java:S1182"})
    public Client clone() {
        Address clonedAddress = (this.address != null) ? this.address.clone() : null;

        List<Phone> clonedPhones = new ArrayList<>();
        if (this.phones != null) {
            for (Phone phone : this.phones) {
                clonedPhones.add(phone.clone());
            }
        }

        Client cloned = new Client(this.id, this.name, clonedAddress, clonedPhones);
        cloned.getPhones().forEach(phone -> phone.setClient(cloned));
        return cloned;
    }

    @Override
    public String toString() {
        return "Client{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
