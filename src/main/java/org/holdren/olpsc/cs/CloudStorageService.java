package org.holdren.olpsc.cs;

import java.io.InputStream;

public interface CloudStorageService
{
    public void store(String path, InputStream inputStream) throws CloudStorageException;
}
