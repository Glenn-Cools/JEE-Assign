package rental;

import java.util.Set;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import rental.Car;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2016-11-15T13:16:23")
@StaticMetamodel(CarRentalCompany.class)
public class CarRentalCompany_ { 

    public static volatile ListAttribute<CarRentalCompany, Car> cars;
    public static volatile SingularAttribute<CarRentalCompany, Set> regions;
    public static volatile SingularAttribute<CarRentalCompany, Set> carTypes;
    public static volatile SingularAttribute<CarRentalCompany, String> name;

}