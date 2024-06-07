package uk.ac.newcastle.enterprisemiddleware.user;

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
public class UserRepository {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    List<User> findAllOrderedByName() {
        TypedQuery<User> query = em.createNamedQuery(User.FIND_ALL, User.class);
        return query.getResultList();
    }

    User findById(Long id) {
        return em.find(User.class, id);
    }

    User findByEmail(String email) {
        TypedQuery<User> query = em.createNamedQuery(User.FIND_BY_EMAIL, User.class).setParameter("email", email);
        return query.getSingleResult();
    }

    List<User> findAllByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        criteria.select(user).where(cb.equal(user.get("name"), name));
        return em.createQuery(criteria).getResultList();
    }

    User create(User user) throws Exception {
        log.info("UserRepository.create() - Creating " + user.getName());

        em.persist(user);

        return user;
    }

    User update(User user) throws Exception {
        log.info("UserRepository.update() - Updating " + user.getName());

        em.merge(user);

        return user;
    }

    User delete(User user) throws Exception {
        log.info("UserRepository.delete() - Deleting " + user.getName());

        if (user.getId() != null) {
            em.remove(em.merge(user));
        } else {
            log.info("UserRepository.delete() - No ID was found so can't Delete.");
        }

        return user;
    }
}
