package uk.ac.newcastle.enterprisemiddleware.restaurant;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class RestaurantRepository {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    List<Restaurant> findAllOrderedByName() {
        TypedQuery<Restaurant> query = em.createNamedQuery(Restaurant.FIND_ALL, Restaurant.class);
        return query.getResultList();
    }

    Restaurant findById(Long id) {
        return em.find(Restaurant.class, id);
    }

    List<Restaurant> findAllByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Restaurant> criteria = cb.createQuery(Restaurant.class);
        Root<Restaurant> restaurant = criteria.from(Restaurant.class);
        criteria.select(restaurant).where(cb.equal(restaurant.get("name"), name));
        return em.createQuery(criteria).getResultList();
    }

    public Restaurant findByPhoneNumber(String phoneNumber) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Restaurant> criteria = cb.createQuery(Restaurant.class);
        Root<Restaurant> restaurant = criteria.from(Restaurant.class);
        criteria.select(restaurant).where(cb.equal(restaurant.get("phoneNumber"), phoneNumber));

        try {
            return em.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    Restaurant create(Restaurant restaurant) throws Exception {
        log.info("RestaurantRepository.create() - Creating " + restaurant.getName());

        em.persist(restaurant);

        return restaurant;
    }

    Restaurant update(Restaurant restaurant) throws Exception {
        log.info("RestaurantRepository.update() - Updating " + restaurant.getName());

        em.merge(restaurant);

        return restaurant;
    }

    Restaurant delete(Restaurant restaurant) throws Exception {
        log.info("RestaurantRepository.delete() - Deleting " + restaurant.getName());

        if (restaurant.getId() != null) {
            em.remove(em.merge(restaurant));
        } else {
            log.info("RestaurantRepository.delete() - No ID was found so can't Delete.");
        }

        return restaurant;
    }

}
