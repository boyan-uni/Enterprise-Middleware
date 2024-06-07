package uk.ac.newcastle.enterprisemiddleware.review;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class ReviewRepository {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    List<Review> findAll() {
        TypedQuery<Review> query = em.createNamedQuery(Review.FIND_ALL, Review.class);
        return query.getResultList();
    }

    public List<Review> findByUserId(Long userId) {
        TypedQuery<Review> query = em.createNamedQuery(Review.FIND_BY_USER, Review.class).setParameter("userId", userId);
        return query.getResultList();
    }

    public List<Review> findByRestaurantId(Long restaurantId) {
        TypedQuery<Review> query = em.createNamedQuery(Review.FIND_BY_RESTAURANT, Review.class).setParameter("restaurantId", restaurantId);
        return query.getResultList();
    }

    Review findById(Long id) {
        return em.find(Review.class, id);
    }

    Review create(Review review) throws Exception {
        log.info("ReviewRepository.create() - Creating review for restaurant " + review.getRestaurant().getName() + " by user " + review.getUser().getName());

        em.persist(review);

        return review;
    }

    Review update(Review review) throws Exception {
        log.info("ReviewRepository.update() - Updating review for restaurant " + review.getRestaurant().getName() + " by user " + review.getUser().getName());

        em.merge(review);

        return review;
    }

    public Review delete(Review review) throws Exception {
        log.info("ReviewRepository.delete() - Deleting review for restaurant " + review.getRestaurant().getName() + " by user " + review.getUser().getName());

        if (review.getId() != null) {
            em.remove(em.merge(review));
        } else {
            log.info("ReviewRepository.delete() - No ID was found so can't Delete.");
        }

        return review;
    }
}
