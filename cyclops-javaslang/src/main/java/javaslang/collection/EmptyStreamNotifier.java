package javaslang.collection;

public class EmptyStreamNotifier extends Throwable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}
