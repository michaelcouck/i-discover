package ikube.discover.experimental;

import ikube.discover.database.model.Analysis;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "wrapper")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value = {Analysis.class})
public class Wrapper<T> {

    @XmlElement(name = "element")
    private List<T> elements = new ArrayList<>();

    public List<T> getElements() {
        return elements;
    }

}
