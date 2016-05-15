@XmlJavaTypeAdapters(@XmlJavaTypeAdapter(value=TimestampAdapter.class,type=Timestamp.class))
package discover.database.model;
import java.sql.Timestamp;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
