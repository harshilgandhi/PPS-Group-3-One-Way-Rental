package rental.sim;

public class Edge {

	public final String source;
	public final String destination;

	public Edge(String source, String destination)
	{
		this.source = source;
		this.destination = destination;
	}

	public Edge reverse()
	{
		return new Edge(destination, source);
	}

	public int hashCode()
	{
		return (source.hashCode() * 123456789) ^
		  (destination.hashCode() * 987654321);
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof Edge))
			return false;
		Edge e = (Edge) obj;
		return e.source.equals(source) &&
		       e.destination.equals(destination);
	}

	public String toString()
	{
		return source + " -> " + destination;
	}
}
