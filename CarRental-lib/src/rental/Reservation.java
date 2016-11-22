package rental;

import javax.persistence.*;

@javax.persistence.Entity
@NamedQueries({
    @NamedQuery(name="Reservation.FindReservationForClient",query="SELECT r FROM Reservation r WHERE r.carRenter LIKE :renter"),
    @NamedQuery(name="Reservation.NumberOfReservationsForClient",query = "SELECT Count(r) FROM Reservation r WHERE r.carRenter LIKE :renter"),
    @NamedQuery(name="Reservation.FindBestClients",query= "SELECT res.carRenter FROM (SELECT r.carRenter , Count(r.carRenter) as number FROM Reservation r GROUP BY r.carRenter)"
            + "WHERE number = Select Max(number) FROM (SELECT r.carRenter , Count(r.carRenter) as number FROM Reservation r GROUP BY r.carRenter)"),
    @NamedQuery(name="Reservation.FindAllRenters",query = "SELECT DISTINCT r.carRenter FROM Reservation r"),
    @NamedQuery(name="Reservation.FindMostPopularCarTypeForCompanyAndPeriod",query=
            "SELECT car.type FROM("
                + "SELECT car.type,Count(r.resId) as number "
                + "FROM"
                    + "(SELECT r.resId,car.type "
                    + "FROM Reservation r"
                    + "JOIN Car car ON r.carId=car.carId "
                    + "WHERE car IN "
                        +" (SELECT car FROM (SELECT c.cars FROM CarRentalCompany c WHERE c.name LIKE :companyName) car)"
                    +" AND r.startDate BETWEEN :beginDate AND :endDate )"
                + " GROUP BY car.type)"
            +"WHERE number = SELECT MAX(number) FROM("
                + "FROM"
                    + "(SELECT r.resId,car.type "
                    + "FROM Reservation r"
                    + "JOIN Car car ON r.carId=car.carId "
                    + "WHERE car IN "
                        +" (SELECT car FROM (SELECT c.cars FROM CarRentalCompany c WHERE c.name LIKE :companyName) car)"
                    +" AND r.startDate BETWEEN :beginDate AND :endDate )"
                + " GROUP BY car.type)"
    )
})
public class Reservation extends Quote {
    
    @Id @GeneratedValue
    private int resId;
    @ManyToOne
    private int carId;
    
    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Reservation(){};

    public Reservation(Quote quote, int carId) {
    	super(quote.getCarRenter(), quote.getStartDate(), quote.getEndDate(), 
    		quote.getRentalCompany(), quote.getCarType(), quote.getRentalPrice());
        this.carId = carId;
    }
    
    /******
     * ID *
     ******/
    
    public int getCarId() {
    	return carId;
    }
    
    public int getResId(){
        return resId;
    }
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), getCarId(), getRentalPrice());
    }	
}