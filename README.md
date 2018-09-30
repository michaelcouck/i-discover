# i-discover

- [installation] (#installation)
    - [maven] (#maven)

### the brains of i-discover

This project is a PoC for extremely high volume indexing and searching. The use case is a banking 
payments engine, that executes 10 000++ tps. The target functionality is to have the transaction data 
indexed and searchable in real time, or at the very least near real time, i.e. < 5 seconds delay to 
consistency.

Assumptions are that there is a last update timestamp available in the transaction data. Also that 
the disks are ssd with > 2500 iops. There must be multicast available on the network for the grid to 
auto discover the nodes.

The reference implementation draws inspiration from the twitter search architecture, in that 
documents are first written to memory where they are constantly re-opened, then to disk when memory fills 
up. Searches are an aggregation over the disk and the memory indexes. The memory indexes constantly re-opened 
to provide near real time indexes over the data.

For integration and performance testing, instructions are written to the database at a rate of 10000 
tps, and the transactions are then indexed at this rate. Secure connections are created for the database, using 
ssh tunneling for security.

Benchmark: 10000 tps on a mac running ubuntu, 16 gig of memory, with cpu < 50%, for 1 000 000 000 transactions

## installation
#### maven
```
mvn install
```