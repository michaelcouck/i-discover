package discover.write;

import discover.grid.DiskWriterEvent;
import discover.grid.IEvent;
import ikube.toolkit.INDEX;

import java.io.IOException;

/**
 * This class writes documents to the indexes, some in memory, and some on the disk.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public class DiskWriter extends Writer<DiskWriterEvent> {

    public DiskWriter() throws IOException {
        super(DiskWriterEvent.class);
        indexWriter = INDEX.getDiskIndexWriter();
    }

    @Override
    public void notify(final IEvent<?, ?> event) {
        DiskWriterEvent diskWriterEvent = (DiskWriterEvent) event;
        writeToIndex(indexWriter, diskWriterEvent.getDocuments());
    }

}