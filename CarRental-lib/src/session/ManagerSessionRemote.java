package session;

import java.util.Set;
import javax.ejb.Remote;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public void registerCompany(String filename);
    
    public void unregisterCompany(String company);
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type);
    
    public Set<String> getBestClients();
    
    public CarType getMostPopularCarType(String company, int year);
      
}