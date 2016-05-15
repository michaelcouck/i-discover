package discover;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-11-2015
 */
public interface IConstants {

    /**
     * This class is to be added to {@link com.google.gson.Gson} so that it doesn't complain when the
     * sub class over rides a field in the super class, for example in the Search class
     * that over rides the annotations in the super class Persistable, the Persistable#id
     * field.
     *
     * @author Michael Couck
     * @version 01.00
     * @since 07-06-2014
     */
    public class IdExclusionStrategy implements ExclusionStrategy {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shouldSkipField(final FieldAttributes fieldAttributes) {
            String fieldName = fieldAttributes.getName();
            Class<?> theClass = fieldAttributes.getDeclaringClass();
            return isFieldInSuperclass(theClass, fieldName);
        }

        private boolean isFieldInSuperclass(final Class<?> subclass, final String fieldName) {
            Field field;
            Class<?> superclass = subclass.getSuperclass();
            while (superclass != null) {
                field = getField(superclass, fieldName);
                if (field != null) {
                    return true;
                }
                superclass = superclass.getSuperclass();
            }
            return false;
        }

        private Field getField(final Class<?> theClass, final String fieldName) {
            try {
                return theClass.getDeclaredField(fieldName);
            } catch (final Exception e) {
                return null;
            }
        }

        @Override
        public boolean shouldSkipClass(final Class<?> aClass) {
            return false;
        }

    }

    String GRID_NAME = "i-discover";

    /**
     * The persistence units' names.
     */
    String PERSISTENCE_UNIT_H2 = "IdiscoverPersistenceUnitH2";

    String API_KEY = "3hBgqJHgsdADILee9gmw3rgmT91tI28Z";

    String ID = "id";

    Gson GSON = new GsonBuilder()
            .addSerializationExclusionStrategy(new IdExclusionStrategy())
            .addDeserializationExclusionStrategy(new IdExclusionStrategy())
            .create();

}
