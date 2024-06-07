package uk.ac.newcastle.enterprisemiddleware.restaurant;

import uk.ac.newcastle.enterprisemiddleware.review.Review;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = Restaurant.FIND_ALL, query = "SELECT r FROM Restaurant r ORDER BY r.name ASC")
})
@XmlRootElement
@Table(name = "restaurants", uniqueConstraints = @UniqueConstraint(columnNames = "phoneNumber"))
public class Restaurant implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String FIND_ALL = "Restaurant.findAll";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[A-Za-z-']+", message = "Please use a name without numbers or specials")
    private String name;

    @NotNull
    @Pattern(regexp = "0[0-9]{10}", message = "Please use a vaild phoneNumber")
    @Column(unique = true)
    private String phoneNumber;

    @NotNull
    @Size(min = 6, max = 6, message = "Postcode size must be 6")
    private String postcode;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant restaurant = (Restaurant) o;
        return phoneNumber.equals(restaurant.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(phoneNumber);
    }
}

