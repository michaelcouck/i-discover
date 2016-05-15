package discover.database.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Base class for entities. All sub classes must declare the inheritance strategy.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@Entity
@SuppressWarnings("serial")
@JsonIgnoreProperties(ignoreUnknown = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(value = {TimestampListener.class})
public abstract class Persistable implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERSISTABLE")
    @SequenceGenerator(name = "PERSISTABLE", sequenceName = "PERSISTABLE", allocationSize = 1000)
    protected long id;

    @Column
    // @Temporal(value = java.util.Date.class)
    private Timestamp timestamp;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}