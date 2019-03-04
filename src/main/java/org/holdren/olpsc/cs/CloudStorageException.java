package org.holdren.olpsc.cs;

public class CloudStorageException extends Exception
{
    public CloudStorageException(String message)
    {
        super(message);
    }

    public CloudStorageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
