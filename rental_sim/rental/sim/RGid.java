package rental.sim;

public class RGid {

	public final int rid;  // relocator id
	public final int gid;  // group of relocator id

	public RGid(int rid, int gid)
	{
		this.rid = rid;
		this.gid = gid;
	}

	public int hashCode()
	{
		return (rid * 123456789) ^
		       (gid * 987654321);
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof RGid))
			return false;
		RGid p = (RGid) obj;
		return rid == p.rid && gid == p.gid;
	}

	public String toString()
	{
		return "" + rid + "(" + gid + ")";
	}
}
