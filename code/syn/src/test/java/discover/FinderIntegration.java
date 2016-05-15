package discover;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FinderIntegration {

    @Spy
    private Finder finder;

    @Test
    public void backup() {
        finder.backup();
    }

}
