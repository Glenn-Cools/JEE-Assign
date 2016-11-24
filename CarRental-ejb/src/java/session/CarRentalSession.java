package session;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.RentalStore;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @PersistenceContext
    protected EntityManager em; //container managed entity manager

    @Override
    public Set<String> getAllRentalCompanies() {
        Query query = em.createNamedQuery("Rental.FindAllCompanyNames", String.class);

        return new HashSet<String>(query.getResultList());
    }

    private HashMap<String, CarRentalCompany> getAllRentals() {
        Query query = em.createNamedQuery("Rental.FindAll", CarRentalCompany.class);
        HashMap<String, CarRentalCompany> rentals = new HashMap<String, CarRentalCompany>();
        List<CarRentalCompany> companies = query.getResultList();

        for (CarRentalCompany crc : companies) {
            rentals.put(crc.getName(), crc);
        }

        return rentals;
    }

    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        for (String crc : getAllRentals().keySet()) {
            for (CarType ct : getAllRentals().get(crc).getAvailableCarTypes(start, end)) {
                if (!availableCarTypes.contains(ct)) {
                    availableCarTypes.add(ct);
                }
            }
        }
        return availableCarTypes;
    }

    @Override
    public Quote createQuote(ReservationConstraints constraints) throws ReservationException {

        Quote out = null;
        for (CarRentalCompany crc : getAllRentals().values()) {
            if (crc.getAllTypes().contains(constraints.getCarType())) {
                try {
                    out = crc.createQuote(constraints, renter);
                    quotes.add(out);
                    break;
                } catch (Exception e) {
                    throw new ReservationException(e);
                }
            }
        }
        return out;
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                done.add(getAllRentals().get(quote.getRentalCompany()).confirmQuote(quote));
            }
        } catch (Exception e) {
            for (Reservation r : done) {
                RentalStore.getRental(r.getRentalCompany()).cancelReservation(r);
            }
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
}
