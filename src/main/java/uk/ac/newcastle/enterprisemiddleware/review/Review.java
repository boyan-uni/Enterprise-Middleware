package uk.ac.newcastle.enterprisemiddleware.review;

import uk.ac.newcastle.enterprisemiddleware.restaurant.Restaurant;
import uk.ac.newcastle.enterprisemiddleware.user.User;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = Review.FIND_ALL, query = "SELECT r FROM Review r ORDER BY r.id ASC"),
        @NamedQuery(name = Review.FIND_BY_USER, query = "SELECT r FROM Review r WHERE r.user.id = :userId"),
        @NamedQuery(name = Review.FIND_BY_RESTAURANT, query = "SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId")
})
@XmlRootElement
@Table(name = "reviews")
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String FIND_ALL = "Review.findAll";
    public static final String FIND_BY_USER = "Review.findByUser";
    public static final String FIND_BY_RESTAURANT = "Review.findByRestaurant";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)        // 不能为空
    private User user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)  // 不能为空，每条评论必须关联到一个用户和一个餐厅
    private Restaurant restaurant;

    @NotNull
    @Size(max = 300)
    private String review;

    @NotNull
    @Min(0)
    @Max(5)
    private int rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        Review review1 = (Review) o;
        return id.equals(review1.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
