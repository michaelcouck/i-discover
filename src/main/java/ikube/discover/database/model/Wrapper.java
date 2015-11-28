package ikube.discover.database.model;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Wrapper")
@XmlSeeAlso(value = {Analysis.class})
public class Wrapper<T> {

    private List<T> elements = new ArrayList<>();

    @XmlAnyElement(lax = true)
    public List<T> getElements() {
        return elements;
    }
}
