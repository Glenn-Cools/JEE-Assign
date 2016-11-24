package session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.*;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.RentalStore;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
        
    @PersistenceContext protected EntityManager em; //container managed entity manager
        
    @Override
    public void registerCompany(String filename){
        CarRentalCompany rental = loadRental(filename);
        em.persist(rental);
    }
    
    @Override
    public void unregisterCompany(String company) throws IllegalArgumentException {
        try{
            CarRentalCompany rental = em.find(CarRentalCompany.class, company);
            em.remove(rental);
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Given company is not in persistent database");
        }
    }
    
    
    public List<CarRentalCompany> getAllRentals(){        
        Query query = em.createNamedQuery("Rental.FindAll", CarRentalCompany.class);
        return query.getResultList();
    }
    
    @Override
    public Set<CarType> getCarTypes(String company){   
        Query query = em.createNamedQuery("Rental.FindAllTypes", CarType.class);
        query.setParameter("companyName", company);
        return new HashSet<CarType>(query.getResultList());
    }
    
   @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        Query companyQuery = em.createNamedQuery("Rental.FindAllCars",Car.class);
        companyQuery.setParameter("companyName", company);
        
        Query resQuery = em.createNamedQuery("Car.FindReservationsForTypeForCars", Reservation.class);
        resQuery.setParameter("cars", companyQuery.getResultList());
        resQuery.setParameter("carType", type);

        return resQuery.getResultList().size();
    }
    
    @Override
    public Set<String> getBestClients(){
        
        Query NBResQuery = em.createNamedQuery("Reservation.FindBestClients",String.class);
           
        return new HashSet<String>(NBResQuery.getResultList());
    }
    
    @Override
    public CarType getMostPopularCarType(String company, int year){
        
        Calendar c = Calendar.getInstance();
        c.set(year, 0, 1);
        Date beginDate = c.getTime();
        c.set(year+1, 0, 1);
        Date endDate =c.getTime();
        
        Query popTypeQuery = em.createNamedQuery("Reservation.FindMostPopularCarTypeForCompanyAndPeriod",CarType.class);
        popTypeQuery.setParameter("companyName", company);
        popTypeQuery.setParameter("beginDate", beginDate);
        popTypeQuery.setParameter("endDate", endDate);
           
        return (CarType) popTypeQuery.getSingleResult();
    }
    
    public static CarRentalCompany loadRental(String datafile) {
        try {
            CrcData data = loadData(datafile);
            CarRentalCompany company = new CarRentalCompany(data.name, new HashSet<String>(data.regions), data.cars);
            //rentals.put(data.name, company);
            Logger.getLogger(RentalStore.class.getName()).log(Level.INFO, "Loaded {0} from file {1}", new Object[]{data.name, datafile});
            return company;
        } catch (NumberFormatException ex) {
            Logger.getLogger(RentalStore.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(RentalStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static CrcData loadData(String datafile)
            throws NumberFormatException, IOException {

        CrcData out = new CrcData();
        StringTokenizer csvReader;
        int nextuid = 0;
       
        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(ManagerSession.class.getClassLoader().getResourceAsStream(datafile)));
        
        try {
            while (in.ready()) {
                String line = in.readLine();
                
                if (line.startsWith("#")) {
                    // comment -> skip					
                } else if (line.startsWith("-")) {
                    csvReader = new StringTokenizer(line.substring(1), ",");
                    out.name = csvReader.nextToken();
                    out.regions = Arrays.asList(csvReader.nextToken().split(":"));
                } else {
                    csvReader = new StringTokenizer(line, ",");
                    //create new car type from first 5 fields
                    CarType type = new CarType(csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()));
                    //create N new cars with given type, where N is the 5th field
                    for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                        out.cars.add(new Car(nextuid++, type));
                    }        
                }
            } 
        } finally {
            in.close();
        }

        return out;
    }
    
    static class CrcData {
            public List<Car> cars = new LinkedList<Car>();
            public String name;
            public List<String> regions =  new LinkedList<String>();
    }

}
