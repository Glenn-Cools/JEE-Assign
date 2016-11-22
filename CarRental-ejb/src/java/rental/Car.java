package rental;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

@javax.persistence.Entity
@NamedQueries({
    @NamedQuery(name="Car.FindReservationsForType", query = "SELECT res FROM (SELECT car.reservations FROM Car c WHERE c.type LIKE :carType) res"),
    @NamedQuery(name="Car.FindReservationsForTypeForCars", query = "SELECT res FROM (SELECT car.reservations FROM Car c IN :cars WHERE c.type LIKE :carType) res")
})
public class Car {

    @Id
    private int id;
    @ManyToOne(fetch=FetchType.EAGER,cascade = CascadeType.PERSIST)
    private CarType type;
    @OneToMany(mappedBy="Reservation.carId",cascade = CascadeType.ALL,fetch=FetchType.EAGER)
    private Set<Reservation> reservations;

    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car(){};
    
    public Car(int uid, CarType type) {
    	this.id = uid;
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }

    /******
     * ID *
     ******/
    
    public int getId() {
    	return id;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    public CarType getType() {
        return type;
    }

    /****************
     * RESERVATIONS *
     ****************/

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }
}