package rabbitHutch;

/**
 * Lesbare namen für die bool Parameter der RabbitMQ API. 
 * 
 * F steht für Flag.
 */
public class F {
	public static boolean autoAck = true;
	public static boolean noAutoAck = false;

	public static boolean durable = true;
	public static boolean nonDurable = false;

	public static boolean exclusive = true;
	public static boolean nonExclusive = false;
	
	public static boolean autoDelete = true;
	public static boolean noAutoDelete = false;
}
