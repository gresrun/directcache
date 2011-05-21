package net.greghaines.directcache;

public class SerializationException extends RuntimeException
{
	private static final long serialVersionUID = -1010188729643637104L;

	public SerializationException()
	{
		super();
	}

	public SerializationException(final String message)
	{
		super(message);
	}

	public SerializationException(final Throwable cause)
	{
		super(cause);
	}

	public SerializationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
