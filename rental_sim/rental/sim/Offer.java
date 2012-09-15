package rental.sim;

import java.util.*;

public class Offer {

	// replies to this offer
	private HashSet <RGid> rgidSet = new HashSet <RGid> ();
	private Vector  <RGid> rgidArr = new Vector  <RGid> ();

	// verifications
	private boolean[] verify = null;

	// access rights handled by the simulator
	boolean requestsAllowed = false;
	boolean verifyAllowed = false;
	boolean seeRequestsAllowed = false;
	boolean seeVerificationsAllowed = false;

	// public information
	public final String src;
	public final String dst;
	public final int group;
	public final int time;

	// initialize offer by giving source, destination and time
	// also give self class to check rules
	public Offer(String src, String dst, int time, Player creator)
	{
		this.src = src;
		this.dst = dst;
		this.time = time;
		this.group = creator.id;
	}

	// add a request for an offer
	// give self class to check rules
	public void request(int rid, Player player) throws Exception
	{
		if (!requestsAllowed)
			throw new Exception("Requests cannot be done at this point");
		if (player.id == group)
			throw new Exception("Request in own offer");
		RGid rgid = new RGid(rid, player.id);
		if (rgidSet.contains(rgid))
			throw new Exception("Request already exists for offer");
		rgidSet.add(rgid);
		rgidArr.add(rgid);
	}

	// return all the requests made for this offer
	public RGid[] requests() throws Exception
	{
		if (!seeRequestsAllowed)
			throw new Exception("Requests cannot be viewed at this point");
		return rgidArr.toArray(new RGid[0]);
	}

	// verify the requests using a boolean array
	// the array correspondes directly to the
	// requests returned by the "requests" function
	public void verify(boolean[] verify, Player creator) throws Exception
	{
		if (!verifyAllowed)
			throw new Exception("Verifications cannot be done at this point");
		if (group != creator.id)
			throw new Exception("Verification by non owner of offer");
		if (verify == null) return;
		if (this.verify != null)
			throw new Exception("Offer has already been verified");
		if (rgidArr.size() != verify.length)
			throw new Exception("Verification includes wrong number of replies");
		this.verify = Arrays.copyOf(verify, verify.length);
	}

	// automatically decline all requests
	// that the player did not reply to
	void denyNonReplied()
	{
		if (verify != null) return;
		verify = new boolean [rgidSet.size()];
		for (int i = 0 ; i != verify.length ; ++i)
			verify[i] = false;
	}

	// get access to verifications posted by owner of the offer
	public boolean[] verifications() throws Exception
	{
		if (!seeVerificationsAllowed)
			throw new Exception("Verifications cannot be viewed at this point");
		return Arrays.copyOf(verify, verify.length);
	}

	public String toString()
	{
		return "" + group + ": "  + src + " -> " + dst + " [" + time + "]";
	}
}
