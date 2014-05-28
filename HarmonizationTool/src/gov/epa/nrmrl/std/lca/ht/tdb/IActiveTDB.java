package gov.epa.nrmrl.std.lca.ht.tdb;

public interface IActiveTDB {
	void addSelectedTDBListener(IActiveTDBListener listener);
    void removeSelectedTDBListener(IActiveTDBListener listener);
}
