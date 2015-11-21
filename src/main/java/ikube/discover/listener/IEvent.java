package ikube.discover.listener;


import ikube.discover.Context;

public interface IEvent<Source, Data> {

    Context getContext();

    Data getData();

    Source getSource();

}
