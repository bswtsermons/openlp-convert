package org.holdren.olpsc.cs;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class DropBoxCloudStorageService implements CloudStorageService
{
    @Autowired
    private DbxClientV2 dbxClient;

    @Override
    public void store(String path, InputStream inputStream) throws CloudStorageException
    {
        try
        {
            dbxClient.files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
        }
        catch (DbxException | IOException e)
        {
            throw new CloudStorageException("error uploading data", e);
        }
    }
}
