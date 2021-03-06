package discover.grid;


import discover.Context;

public interface IEvent<Source, Data> {

    Context getContext();

    Data getData();

    Source getSource();

}
